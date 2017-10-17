package uk.org.tombolo.importer.londondatastore;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.*;

import java.net.URL;
import java.util.*;

/**
 * Importer for the Public Health Outcomes Framework (PHOF) indicators for London.
 *
 * URL: http://data.london.gov.uk/dataset/public-health-outcomes-framework-indicators
 *
 *
 */
public class LondonPHOFImporter extends AbstractLondonDatastoreImporter {
    Logger log = LoggerFactory.getLogger(LondonPHOFImporter.class);
    private static final String DATAFILE_SUFFIX = ".xlsx";
    private static final String DATAFILE
            = "https://files.datapress.com/london/dataset/public-health-outcomes-framework-indicators/2015-11-10T12:05:53/phof-indicators-data-london-borough.xlsx";

    private enum DatasourceId {
        phofIndicatorsLondonBorough(new DatasourceSpec(
                LondonPHOFImporter.class,
                "phofIndicatorsLondonBorough",
                "PHOF Indicators London Borough",
                "Public Health Outcomes Framework Indicators for London Boroughs",
                "http://data.london.gov.uk/dataset/public-health-outcomes-framework-indicators"
        ));

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }

    }

    ExcelUtils excelUtils = new ExcelUtils();

    Workbook workbook = null;

    public LondonPHOFImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public void setDownloadUtils(DownloadUtils downloadUtils) {
        super.setDownloadUtils(downloadUtils);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope,  List<String> datasourceLocation) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
        RowCellExtractor attributeNameExtractor = new RowCellExtractor(0, CellType.STRING);
        RowCellExtractor subjectExtractor = new RowCellExtractor(4, CellType.STRING);
        RowCellExtractor timestampExtractor = new RowCellExtractor(1, CellType.STRING);
        RowCellExtractor valueExtractor = new RowCellExtractor(6, CellType.NUMERIC);

        if (null == getWorkbook()) {
            setWorkbook(excelUtils.getWorkbook(
                    downloadUtils.fetchInputStream(new URL(DATAFILE), getProvider().getLabel(), DATAFILE_SUFFIX)));
        }

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

            String attributeLabel = attributeNameExtractor.extract();
            TimedValueExtractor timedValueExtractor = new TimedValueExtractor(
                    getProvider(),
                    subjectType,
                    subjectExtractor,
                    new ConstantExtractor(attributeLabel),
                    timestampExtractor,
                    valueExtractor
            );
            try {
                timedValueBuffer.add(timedValueExtractor.extract());
            }catch (UnknownSubjectLabelException e){
                // No worries if the subject does not exist
            }catch (ExtractorException e){
                log.warn(e.getMessage());
            }
        }
        workbook.close();
        saveAndClearTimedValueBuffer(timedValueBuffer);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) throws Exception {
        RowCellExtractor attributeNameExtractor = new RowCellExtractor(0, CellType.STRING);

        if (null == getWorkbook()) {
            setWorkbook(excelUtils.getWorkbook(
                    downloadUtils.fetchInputStream(new URL(DATAFILE), getProvider().getLabel(), DATAFILE_SUFFIX)));
        }

        Map<String, Attribute> attributes = new HashMap<>();
        Sheet sheet = workbook.getSheetAt(3);
        Iterator<Row> rowIterator = sheet.iterator();
        Row header = rowIterator.next();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();

            attributeNameExtractor.setRow(row);
            String attributeLabel = attributeNameExtractor.extract();

            if (!attributes.containsKey(attributeLabel))
                attributes.put(
                        attributeLabel,
                        new Attribute(getProvider(), attributeLabel, attributeLabel)
                );
        }
        workbook.close();
        return new ArrayList<>(attributes.values());
    }
}
