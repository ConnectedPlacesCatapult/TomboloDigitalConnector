package uk.org.tombolo.importer.phe;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Importer for importing childhood obesity data.
 */
public class ChildhoodObesityImporter extends AbstractPheImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {childhoodObesity};
    private enum GeographyLabel {msoa, ward, la};
    private enum TemporalLabel {y2014};
    private String[] dataSourceName = {"MSOA Childhood Obesity", "Local AuthorityChildhoodObesity", "Ward ChildhoodObesity"};
    private String[] dataSourceDesc = {"MSOA Childhood Obesity", "Local AuthorityChildhoodObesity", "Ward ChildhoodObesity"};

    private static final String DATASOURCE_SUFFIX = ".xlsx";
    private static final String DATASOURCE = "http://www.noo.org.uk/securefiles/161024_1352/20150511_MSOA_Ward_Obesity.xlsx";

    private enum AttributeLabel {receptionNumberMeasured, year6NumberMeasured,
        receptionNumberObese, receptionPercentageObese,
        receptionPercentageObeseLowerLimit, receptionPercentageObeseUpperLimit,
        year6NumberObese, year6PercentageObese,
        year6PercentageObeseLowerLimit, year6PercentageObeseUpperLimit,
        receptionNumberExcessWeight, receptionPercentageExcessWeight,
        receptionPercentageExcessWeightLowerLimit, receptionPercentageExcessWeightUpperLimit,
        year6NumberExcessWeight, year6PercentageExcessWeight,
        year6PercentageExcessWeightLowerLimit, year6PercentageExcessWeightUpperLimit
    };

    private ExcelUtils excelUtils = new ExcelUtils();;

    public ChildhoodObesityImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
        geographyLabels = stringsFromEnumeration(GeographyLabel.class);
        temporalLabels = stringsFromEnumeration(TemporalLabel.class);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DatasourceId datasourceLabel = DatasourceId.valueOf(datasourceId);
        Datasource datasource = new Datasource(
                getClass(),
                datasourceId,
                getProvider(),
                "Childhood Obesity",
                "");

        datasource.setUrl("https://www.noo.org.uk/");

        datasource.addAllTimedValueAttributes(getAttributes());

        return datasource;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
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
                    subjectType = OaImporter.getSubjectType(OaImporter.OaType.localAuthority);
                    break;
                case msoa:
                    sheet = workbook.getSheet("MSOAData_2011-12_2013-14");
                    subjectType = OaImporter.getSubjectType(OaImporter.OaType.msoa);
                    break;
                case ward:
                    throw new Error("Wards are not yet supported");
                    // FIXME: In case we want to suport wards at some point, here is the sheet to use
                    //sheet = workbook.getSheet("WardData_2011-12_2013-14");
                    //break;
            }
            if (sheet == null)
                throw new Error("Sheet not found for datasource: " + datasource.getId());

            // Define a list of timed value extractor, one for each attribute
            List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();

            RowCellExtractor subjectExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);
            ConstantExtractor timestampExtractor = new ConstantExtractor(year);

            for (AttributeLabel attributeLabel : AttributeLabel.values()) {
                ConstantExtractor attributeExtractor = new ConstantExtractor(attributeLabel.name());
                RowCellExtractor valueExtractor = new RowCellExtractor(getAttributeColumnId(geographyLabel, attributeLabel), Cell.CELL_TYPE_NUMERIC);
                timedValueExtractors.add(new TimedValueExtractor(getProvider(), subjectType, subjectExtractor, attributeExtractor, timestampExtractor, valueExtractor));
            }

            // Extract timed values
            excelUtils.extractAndSaveTimedValues(sheet, this, timedValueExtractors);
        }
    }

    private List<Attribute> getAttributes(){
        List<Attribute> attributes = new ArrayList<>();
        // Obesity at reception
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionNumberMeasured.name(), "Number Measured at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionNumberObese.name(), "Number Obese at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionPercentageObese.name(), "Percentage Obese at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionPercentageObeseLowerLimit.name(), "Lower Limit of Percentage Obese at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionPercentageObeseUpperLimit.name(), "Upper Limit of Percentage Obese at Reception",null, Attribute.DataType.numeric));

        // Obesity at year 6
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6NumberMeasured.name(), "Number Measured at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6NumberObese.name(), "Number Obese at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6PercentageObese.name(), "Percentage Obese at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6PercentageObeseLowerLimit.name(), "Lower Limit of Percentage Obese at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6PercentageObeseUpperLimit.name(), "Upper Limit of Percentage Obese at Year 6",null, Attribute.DataType.numeric));

        // Excess weight at reception
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionNumberExcessWeight.name(), "Number Excess Weight at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionPercentageExcessWeight.name(), "Percentage Excess Weight at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionPercentageExcessWeightLowerLimit.name(), "Lower Limit of Percentage Excess Weight at Reception",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.receptionPercentageExcessWeightUpperLimit.name(), "Upper Limit of Percentage  Excess Weight at Reception",null, Attribute.DataType.numeric));

        // Excess weight at year 6
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6NumberExcessWeight.name(), "Number Excess Weight at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6PercentageExcessWeight.name(), "Percentage Excess Weight at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6PercentageExcessWeightLowerLimit.name(), "Lower Limit of Percentage Excess Weight at Year 6",null, Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.year6PercentageExcessWeightUpperLimit.name(), "Upper Limit of Percentage Excess Weight at Year 6",null, Attribute.DataType.numeric));

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
