package uk.org.tombolo.importer.phe;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Importer for importing childhood obesity data.
 */
public class ChildhoodObesityImporter extends AbstractPheImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {msoaChildhoodObesity2014, laChildhoodObesity2014, wardChildhoodObesity2014};
    private String[] dataSourceName = {"MSOA Childhood Obesity", "Local AuthorityChildhoodObesity", "Ward ChildhoodObesity"};
    private String[] dataSourceDesc = {"MSOA Childhood Obesity", "Local AuthorityChildhoodObesity", "Ward ChildhoodObesity"};

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

    private ExcelUtils excelUtils;

    private static final int timedValueBufferSize = 100000;

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        List<Datasource> datasources = new ArrayList<>();
        for(DatasourceId datasourceId : DatasourceId.values()){
            datasources.add(getDatasource(datasourceId.name()));
        }
        return datasources;
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DatasourceId datasourceIdEnum = DatasourceId.valueOf(datasourceId);
        Datasource datasource = new Datasource(
                datasourceId,
                getProvider(),
                dataSourceName[datasourceIdEnum.ordinal()],
                dataSourceDesc[datasourceIdEnum.ordinal()]);

        datasource.setUrl("https://www.noo.org.uk/");
        datasource.setRemoteDatafile("http://www.noo.org.uk/securefiles/161024_1352/20150511_MSOA_Ward_Obesity.xlsx");
        datasource.setLocalDatafile("PublicHealthEngland/20150511_MSOA_Ward_Obesity.xlsx");

        datasource.addAllTimedValueAttributes(getAttributes());

        return datasource;
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        if (excelUtils == null)
            initalize();

        int valueCount = 0;
        List<TimedValue> timedValueBuffer = new ArrayList<>();

        // Save Provider and Attributes
        saveProviderAndAttributes(datasource);

        // Choose the apppropriate workbook sheet
        Workbook workbook = excelUtils.getWorkbook(datasource);
        Sheet sheet = null;
        String year = "2014";
        DatasourceId datasourceId = DatasourceId.valueOf(datasource.getId());
        switch (datasourceId) {
            case laChildhoodObesity2014:
                sheet = workbook.getSheet("LAData_2011-12_2013-14");
                break;
            case msoaChildhoodObesity2014:
                sheet = workbook.getSheet("MSOAData_2011-12_2013-14");
                break;
            case wardChildhoodObesity2014:
                sheet = workbook.getSheet("WardData_2011-12_2013-14");
                break;
        }
        if (sheet == null)
            throw new Error("Sheet not found for datasource: " + datasource.getId());

        // Define a list of timed value extractor, one for each attribute
        List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();

        RowCellExtractor subjectExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);
        ConstantExtractor timestampExtractor = new ConstantExtractor(year);

        for (AttributeLabel attributeLabel : AttributeLabel.values()) {
            ConstantExtractor attributeExtractor = new ConstantExtractor(attributeLabel.name());
            RowCellExtractor valueExtractor = new RowCellExtractor(getAttributeColumnId(datasourceId, attributeLabel), Cell.CELL_TYPE_NUMERIC);
            timedValueExtractors.add(new TimedValueExtractor(getProvider(), subjectExtractor, attributeExtractor, timestampExtractor, valueExtractor));
        }

        // Extract timed values
        for (int rowId = 0; rowId < sheet.getLastRowNum()+1; rowId++) {
            Row row = sheet.getRow(rowId);
            for (TimedValueExtractor extractor : timedValueExtractors) {
                subjectExtractor.setRow(row);
                ((RowCellExtractor) extractor.getValueExtractor()).setRow(row);
                try {
                    TimedValue timedValue = extractor.extract();
                    timedValueBuffer.add(timedValue);
                    valueCount++;
                    if (valueCount % timedValueBufferSize == 0) {
                        // Buffer is full ... we write values to db
                        saveBuffer(timedValueBuffer, valueCount);
                    }
                }catch (BlankCellException e){
                    // We ignore this since there may be multiple blank cells in the data without having to worry
                }catch (ExtractorException e){
                    log.warn("Could not extract value: {}",e.getMessage());
                }
            }
        }
        saveBuffer(timedValueBuffer, valueCount);

        return valueCount;
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

    private int getAttributeColumnId(DatasourceId datasourceId, AttributeLabel attributeLabel){
        int base = 0;
        switch (datasourceId){
            case msoaChildhoodObesity2014:
                base = 3;
                break;
            case laChildhoodObesity2014:
                base = 2;
                break;
            case wardChildhoodObesity2014:
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

    private void initalize(){
        excelUtils = new ExcelUtils(downloadUtils);
    }

    private static void saveBuffer(List<TimedValue> timedValueBuffer, int valueCount){
        log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
        TimedValueUtils.save(timedValueBuffer);
        timedValueBuffer.clear();
        log.info("Total values written: {}", valueCount);
    }
}
