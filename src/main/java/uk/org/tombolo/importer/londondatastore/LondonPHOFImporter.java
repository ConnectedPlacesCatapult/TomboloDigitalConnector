package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.*;

import java.util.*;

/**
 * Importer for the Public Health Outcomes Framework (PHOF) indicators for London.
 *
 * http://data.london.gov.uk/dataset/public-health-outcomes-framework-indicators
 */
public class LondonPHOFImporter extends AbstractLondonDatastoreImporter implements Importer {
    private enum DatasourceLabel {phofIndicatorsLondonBorough};
    Logger log = LoggerFactory.getLogger(LondonPHOFImporter.class);

    ExcelUtils excelUtils;

    public LondonPHOFImporter(){
        super();
        datasourceIds = stringsFromEnumeration(DatasourceLabel.class);
    }

    @Override
    public void setDownloadUtils(DownloadUtils downloadUtils) {
        super.setDownloadUtils(downloadUtils);
        excelUtils = new ExcelUtils(downloadUtils);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceLabel datasourceLabel = DatasourceLabel.valueOf(datasourceIdString);
        switch (datasourceLabel){
            case phofIndicatorsLondonBorough:
                Datasource datasource = new Datasource(
                        getClass(),
                        datasourceLabel.name(),
                        getProvider(),
                        "PHOF Indicators London Borough",
                        "Public Health Outcomes Framework Indicators for London Boroughs"
                );
                datasource.setUrl("http://data.london.gov.uk/dataset/public-health-outcomes-framework-indicators");
                datasource.setRemoteDatafile("https://files.datapress.com/london/dataset/public-health-outcomes-framework-indicators/2015-11-10T12:05:53/phof-indicators-data-london-borough.xlsx");
                datasource.setLocalDatafile("LondonDatastore/phof-indicators-data-london-borough.xlsx");

                datasource.addAllTimedValueAttributes(getAttributes(datasource));
                return datasource;
            default:
                throw new ConfigurationException("Datasource not found: " + datasourceIdString);
        }
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

        RowCellExtractor attributeNameExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);
        RowCellExtractor subjectExtractor = new RowCellExtractor(4, Cell.CELL_TYPE_STRING);
        RowCellExtractor timestampExtractor = new RowCellExtractor(1, Cell.CELL_TYPE_STRING);
        RowCellExtractor valueExtractor = new RowCellExtractor(6, Cell.CELL_TYPE_NUMERIC);

        Workbook workbook = excelUtils.getWorkbook(datasource);

        List<TimedValue> timedValueBuffer = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(3);
        Iterator<Row> rowIterator = sheet.iterator();
        Row header = rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            attributeNameExtractor.setRow(row);
            subjectExtractor.setRow(row);
            timestampExtractor.setRow(row);
            valueExtractor.setRow(row);

            String attributeLabel = nameToLabel(attributeNameExtractor.extract());
            TimedValueExtractor timedValueExtractor = new TimedValueExtractor(
                    getProvider(),
                    subjectExtractor,
                    new ConstantExtractor(attributeLabel),
                    timestampExtractor,
                    valueExtractor
            );
            try {
                timedValueBuffer.add(timedValueExtractor.extract());
                timedValueCount++;
            }catch (UnknownSubjectLabelException e){
                // No worries if the subject does not exist
            }catch (ExtractorException e){
                log.warn(e.getMessage());
            }
        }
        workbook.close();
        saveBuffer(timedValueBuffer, timedValueCount);
    }

    private List<Attribute> getAttributes(Datasource datasource) throws Exception {
        RowCellExtractor attributeNameExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);

        Workbook workbook = excelUtils.getWorkbook(datasource);

        Map<String, Attribute> attributes = new HashMap<>();
        Sheet sheet = workbook.getSheetAt(3);
        Iterator<Row> rowIterator = sheet.iterator();
        Row header = rowIterator.next();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();

            attributeNameExtractor.setRow(row);
            String attributeName = attributeNameExtractor.extract();
            String attributeLabel = nameToLabel(attributeName);

            if (!attributes.containsKey(attributeLabel))
                attributes.put(
                        attributeLabel,
                        new Attribute(getProvider(),attributeLabel, attributeName, attributeName, Attribute.DataType.numeric)
                );
        }
        workbook.close();
        return new ArrayList<>(attributes.values());
    }

    private String nameToLabel(String name){
        return DigestUtils.md5Hex(name);
    }
}
