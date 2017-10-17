package uk.org.tombolo.importer.londondatastore;

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
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.phe.AbstractPheImporter;
import uk.org.tombolo.importer.phe.ChildhoodObesityImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for importing the adult self reported obesity from the London datastore.
 *
 */
public class AdultObesityImporter extends AbstractLondonDatastoreImporter {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {
        adultObesity(new DatasourceSpec(
                AdultObesityImporter.class,
                "adultObesity",
                "Local Authority Adult Obesity",
                "Self reported adult obesity",
                "https://data.london.gov.uk/dataset/obesity-adults")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    private static final String DATASOURCE_SUFFIX = ".xls";
    private static final String DATASOURCE = "https://files.datapress.com/london/dataset/obesity-adults/population-bmi-classification-london.xls";

    private ExcelUtils excelUtils = new ExcelUtils();

    private enum AttributeLabel {fractionUnderweight,fractionHealthyWeight,fractionOverweight,fractionObese,fractionExcessWeight}

    public AdultObesityImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;

    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // Choose the apppropriate workbook sheet
        Workbook workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(new URL(DATASOURCE), getProvider().getLabel(), DATASOURCE_SUFFIX));
        Sheet sheet = workbook.getSheet("Active People Survey");
        String year = "2013";

        List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();
        RowCellExtractor subjectExtractor = new RowCellExtractor(0, CellType.STRING);
        ConstantExtractor timestampExtractor = new ConstantExtractor(year);

        SubjectType subjectType = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
        for (AttributeLabel attributeLabel : AttributeLabel.values()){
            ConstantExtractor attributeExtractor = new ConstantExtractor(attributeLabel.name());
            RowCellExtractor valueExtractor
                    = new RowCellExtractor(getAttributeColumnId(attributeLabel), CellType.NUMERIC);
            timedValueExtractors.add(new TimedValueExtractor(
                    getProvider(),
                    subjectType,
                    subjectExtractor,
                    attributeExtractor,
                    timestampExtractor,
                    valueExtractor));
        }
        excelUtils.extractAndSaveTimedValues(sheet, this, timedValueExtractors);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionUnderweight.name(),
                "BMI less than 18.5kg/m2"));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionHealthyWeight.name(),
                "BMI greater than or equal to 18.5 but less than 25kg/m2"));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionOverweight.name(),
                "BMI greater than or equal to 25 but less than 30kg/m2"));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionObese.name(),
                "BMI greater than or equal to 30kg/m2"));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionExcessWeight.name(),
                "BMI greater than or equal to 25kg/m2 (overweight including obese)"));
        return attributes;
    }

    private int getAttributeColumnId(AttributeLabel attributeLabel){
        switch (attributeLabel){
           case fractionUnderweight:
               return 2;
            case fractionHealthyWeight:
                return 6;
            case fractionOverweight:
                return 10;
            case fractionObese:
                return 14;
            case fractionExcessWeight:
                return 18;
            default:
                throw new Error("Unknown attribute label: " + String.valueOf(attributeLabel));
        }
    }
}
