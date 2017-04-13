package uk.org.tombolo.importer.phe;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for importing the adult self reported obesity from the PHE NOO website.
 *
 * http://www.noo.org.uk/visualisation
 */
public class AdultObesityImporter extends AbstractPheImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {adultObesity};
    private Datasource[] datasources = {
        new Datasource(
                getClass(),
                DatasourceId.adultObesity.name(),
                getProvider(),
                "Local Authority Adult Obesity",
                "Self reported adult obesity")
    };

    private static final String DATASOURCE_SUFFIX = ".xlsx";
    private static final String DATASOURCE = "https://www.noo.org.uk/gsf.php5?f=314008&fv=21761";

    private ExcelUtils excelUtils = new ExcelUtils();

    private enum AttributeLabel {fractionUnderweight,fractionHealthyWeight,fractionOverweight,fractionObese,fractionExcessWeight}

    public AdultObesityImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case adultObesity:
                Datasource datasource = datasources[datasourceId.ordinal()];
                datasource.setUrl("http://www.noo.org.uk/visualisation");
                datasource.addAllTimedValueAttributes(getAttributes());
                return datasource;
            default:
                throw new Error("Unknown datasource");
        }
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        // Choose the apppropriate workbook sheet
        Workbook workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(new URL(DATASOURCE), getProvider().getLabel(), DATASOURCE_SUFFIX));
        Sheet sheet = workbook.getSheetAt(1);
        String year = "2014";

        List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();
        RowCellExtractor subjectExtractor = new RowCellExtractor(1, Cell.CELL_TYPE_STRING);
        ConstantExtractor timestampExtractor = new ConstantExtractor(year);

        for (AttributeLabel attributeLabel : AttributeLabel.values()){
            ConstantExtractor attributeExtractor = new ConstantExtractor(attributeLabel.name());
            RowCellExtractor valueExtractor
                    = new RowCellExtractor(getAttributeColumnId(attributeLabel), Cell.CELL_TYPE_NUMERIC);
            timedValueExtractors.add(new TimedValueExtractor(
                    getProvider(),
                    subjectExtractor,
                    attributeExtractor,
                    timestampExtractor,
                    valueExtractor));
        }
        excelUtils.extractAndSaveTimedValues(sheet, this, timedValueExtractors, BUFFER_THRESHOLD);
    }

    private List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionUnderweight.name(),
                "Fraction Underweight", "BMI less than 18.5kg/m2",
                Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionHealthyWeight.name(),
                "Fraction Healty Weight", "BMI greater than or equal to 18.5 but less than 25kg/m2",
                Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionOverweight.name(),
                "Fraction Overweight", "BMI greater than or equal to 25 but less than 30kg/m2",
                Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionObese.name(),
                "Fraction Obese", "BMI greater than or equal to 30kg/m2",
                Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(), AttributeLabel.fractionExcessWeight.name(),
                "Fraction Excess Weight", "BMI greater than or equal to 25kg/m2 (overweight including obese)",
                Attribute.DataType.numeric));
        return attributes;
    }

    private int getAttributeColumnId(AttributeLabel attributeLabel){
        switch (attributeLabel){
           case fractionUnderweight:
               return 6;
            case fractionHealthyWeight:
                return 10;
            case fractionOverweight:
                return 14;
            case fractionObese:
                return 18;
            case fractionExcessWeight:
                return 22;
            default:
                throw new Error("Unknown attribute label: " + String.valueOf(attributeLabel));
        }
    }
}
