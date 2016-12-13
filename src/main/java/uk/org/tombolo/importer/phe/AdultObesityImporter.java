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

import java.util.ArrayList;
import java.util.List;

/**
 * Importer for importing the adult self reported obesity from the PHE NOO website.
 *
 * http://www.noo.org.uk/visualisation
 */
public class AdultObesityImporter extends AbstractPheImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {laAdultObesity2014};
    private Datasource[] datasources = {
        new Datasource(
                DatasourceId.laAdultObesity2014.name(),
                getProvider(),
                "Local Authority Adult Obesity",
                "Self reported adult obesity")
    };

    private ExcelUtils excelUtils;

    private enum AttributeLabel {fractionUnderweight,fractionHealthyWeight,fractionOverweight,fractionObese,fractionExcessWeight}

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return datasourcesFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case laAdultObesity2014:
                Datasource datasource = datasources[datasourceId.ordinal()];
                datasource.setUrl("http://www.noo.org.uk/visualisation");
                datasource.setRemoteDatafile("https://www.noo.org.uk/gsf.php5?f=314008&fv=21761");
                datasource.setLocalDatafile("/PublicHealthEngland/BMI_categories_2012-2014.xlsx");
                datasource.addAllTimedValueAttributes(getAttributes());
                return datasource;
            default:
                throw new Error("Unknown datasource");
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        if (excelUtils == null)
            initalize();

        // Save Provider and Attributes
        saveDatasourceMetadata(datasource);

        // Choose the apppropriate workbook sheet
        Workbook workbook = excelUtils.getWorkbook(datasource);
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
        return excelUtils.extractTimedValues(sheet, this, timedValueExtractors, BUFFER_THRESHOLD);
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

    private void initalize(){
        excelUtils = new ExcelUtils(downloadUtils);
    }
}
