package uk.org.tombolo.importer.phe;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Importer for importing childhood obesity data.
 */
public class ChildhoodObesityImporter extends AbstractPheImporter {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {
        childhoodObesity(new DatasourceSpec(
                ChildhoodObesityImporter.class,
                "childhoodObesity",
                "Childhood Obesity",
                "",
                "https://www.noo.org.uk/")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    private enum GeographyLabel {msoa, ward, la}
    private enum TemporalLabel {y2014}

    private static final String DATASOURCE_SUFFIX = ".xlsx";
    private static final String DATASOURCE = "http://www.noo.org.uk/securefiles/161024_1352/20150511_MSOA_Ward_Obesity.xlsx";

    private enum AttributeLabel {
        // Obesity at reception
        receptionNumberMeasured("Number Measured at Reception"),
        year6NumberMeasured("Number Obese at Reception"),
        receptionNumberObese("Percentage Obese at Reception"),
        receptionPercentageObese("Lower Limit of Percentage Obese at Reception"),
        receptionPercentageObeseLowerLimit("Upper Limit of Percentage Obese at Reception"),
        // Obesity at year 6
        receptionPercentageObeseUpperLimit("Number Measured at Year 6"),
        year6NumberObese("Number Obese at Year 6"),
        year6PercentageObese("Percentage Obese at Year 6"),
        year6PercentageObeseLowerLimit("Lower Limit of Percentage Obese at Year 6"),
        year6PercentageObeseUpperLimit("Upper Limit of Percentage Obese at Year 6"),
        // Excess weight at reception
        receptionNumberExcessWeight("Number Excess Weight at Reception"),
        receptionPercentageExcessWeight("Percentage Excess Weight at Reception"),
        receptionPercentageExcessWeightLowerLimit("Lower Limit of Percentage Excess Weight at Reception"),
        receptionPercentageExcessWeightUpperLimit("Upper Limit of Percentage Excess Weight at Reception"),
        // Excess weight at year 6
        year6NumberExcessWeight("Number Excess Weight at Year 6"),
        year6PercentageExcessWeight("Percentage Excess Weight at Year 6"),
        year6PercentageExcessWeightLowerLimit("Lower Limit of Percentage Excess Weight at Year 6"),
        year6PercentageExcessWeightUpperLimit("Upper Limit of Percentage Excess Weight at Year 6");

        private String description;
        AttributeLabel(String description) { this.description = description; }
    };

    private ExcelUtils excelUtils = new ExcelUtils();;

    public ChildhoodObesityImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
        geographyLabels = stringsFromEnumeration(GeographyLabel.class);
        temporalLabels = stringsFromEnumeration(TemporalLabel.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope,  List<String> datasourceLocation) throws Exception {
        // Choose the apppropriate workbook sheet
        Workbook workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(new URL(DATASOURCE), getProvider().getLabel(), DATASOURCE_SUFFIX)
        );
        Sheet sheet = null;
        SubjectType subjectType = null;
        String year = "2014";
        for (String geographyScopeString : geographyScope) {
            GeographyLabel geographyLabel = GeographyLabel.valueOf(geographyScopeString);
            switch (geographyLabel) {
                case la:
                    sheet = workbook.getSheet("LAData_2011-12_2013-14");
                    subjectType = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                            OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
                    break;
                case msoa:
                    sheet = workbook.getSheet("MSOAData_2011-12_2013-14");
                    subjectType = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                            OaImporter.OaType.msoa.name(), OaImporter.OaType.msoa.datasourceSpec.getDescription());
                    break;
                case ward:
                    throw new Error("Wards are not yet supported");
                    // FIXME: In case we want to suport wards at some point, here is the sheet to use
                    //sheet = workbook.getSheet("WardData_2011-12_2013-14");
                    //break;
            }
            if (sheet == null)
                throw new Error("Sheet not found for datasource: " + datasource.getDatasourceSpec().getId());

            // Define a list of timed value extractor, one for each attribute
            List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();

            RowCellExtractor subjectExtractor = new RowCellExtractor(0, CellType.STRING);
            ConstantExtractor timestampExtractor = new ConstantExtractor(year);

            for (AttributeLabel attributeLabel : AttributeLabel.values()) {
                ConstantExtractor attributeExtractor = new ConstantExtractor(attributeLabel.name());
                RowCellExtractor valueExtractor = new RowCellExtractor(getAttributeColumnId(geographyLabel, attributeLabel), CellType.NUMERIC);
                timedValueExtractors.add(new TimedValueExtractor(getProvider(), subjectType, subjectExtractor, attributeExtractor, timestampExtractor, valueExtractor));
            }

            // Extract timed values
            excelUtils.extractAndSaveTimedValues(sheet, this, timedValueExtractors);
        }
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        List<Attribute> attributes = new ArrayList<>();
        Arrays.stream(AttributeLabel.values()).map(attributeLabel ->
                new Attribute(getProvider(), attributeLabel.name(), attributeLabel.description)).forEach(attributes::add);
        return attributes;
    }

    private int getAttributeColumnId(GeographyLabel geographyLabel, AttributeLabel attributeLabel){
        int base = 0;
        switch (geographyLabel){
            case msoa:
                base = 3;
                break;
            case la:
                base = 2;
                break;
            case ward:
                base = 4;
                break;
        }

        switch (attributeLabel){
            case receptionNumberMeasured:
                return base;
            case receptionNumberObese:
                return base+1;
            case receptionPercentageObese:
                return base+2;
            case receptionPercentageObeseLowerLimit:
                return base+3;
            case receptionPercentageObeseUpperLimit:
                return base+4;
            case year6NumberMeasured:
                return base+6;
            case year6NumberObese:
                return base+7;
            case year6PercentageObese:
                return base+8;
            case year6PercentageObeseLowerLimit:
                return base+9;
            case year6PercentageObeseUpperLimit:
                return base+10;
            case receptionNumberExcessWeight:
                return base+13;
            case receptionPercentageExcessWeight:
                return base+14;
            case receptionPercentageExcessWeightLowerLimit:
                return base+15;
            case receptionPercentageExcessWeightUpperLimit:
                return base+16;
            case year6NumberExcessWeight:
                return base+19;
            case year6PercentageExcessWeight:
                return base+20;
            case year6PercentageExcessWeightLowerLimit:
                return base+21;
            case year6PercentageExcessWeightUpperLimit:
                return base+22;
            default:
                throw new Error("Unknown attribute label: " + String.valueOf(attributeLabel));
        }
    }
}
