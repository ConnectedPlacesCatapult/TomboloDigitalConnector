package uk.org.tombolo.importer.londondatastore;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Importer for importing the walking and cycling statistics for boroughs.
 */
public class WalkingCyclingBoroughImporter extends AbstractLondonDatastoreImporter implements Importer{
    Logger log = LoggerFactory.getLogger(WalkingCyclingBoroughImporter.class);
    public enum DatasourceId {walkingCyclingBorough};
    public enum AttributeId {walk5xWeek,cycle1xWeek};

    ExcelUtils excelUtils;

    @Override
    public void setDownloadUtils(DownloadUtils downloadUtils) {
        super.setDownloadUtils(downloadUtils);
        excelUtils = new ExcelUtils(downloadUtils);
    }

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        List<Datasource> datasources = new ArrayList<>();
        for(DatasourceId datasourceId : DatasourceId.values()){
            datasources.add(getDatasource(datasourceId.name()));
        }
        return datasources;
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case walkingCyclingBorough:
                Datasource datasource = new Datasource(
                        DatasourceId.walkingCyclingBorough.name(),
                        getProvider(),
                        "Walking and Cycling in London Boroughs",
                        "Walking and Cycling in London Boroughs"
                );

                datasource.setUrl("http://data.london.gov.uk/dataset/walking-and-cycling-borough");
                datasource.setRemoteDatafile("https://files.datapress.com/london/dataset/walking-and-cycling-borough/walking-cycling-borough.xls");
                datasource.setLocalDatafile("/LondonDatastore/walking-and-cycling-borough.xls");

                for (AttributeId attributeId : AttributeId.values()) {
                    datasource.addTimedValueAttribute(getAttribute(attributeId));
                }

                return datasource;
            default:
                return null;
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        saveProviderAndAttributes(datasource);

        Workbook workbook = excelUtils.getWorkbook(datasource);
        RowCellExtractor subjectLabelExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);
        List<TimedValue> timedValueBuffer = new ArrayList<>();

        // Extract walking
        ConstantExtractor walk5xWeekAttributeLabelExtractor = new ConstantExtractor(AttributeId.walk5xWeek.name());
        List<TimedValueExtractor> walk5xWeekExtractors = new ArrayList<>();
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2011-12-31T23:59:59"),
                new RowCellExtractor(7,Cell.CELL_TYPE_NUMERIC)
        ));
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2012-12-31T23:59:59"),
                new RowCellExtractor(18, Cell.CELL_TYPE_NUMERIC)
        ));
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2013-12-31T23:59:59"),
                new RowCellExtractor(29, Cell.CELL_TYPE_NUMERIC)
        ));
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2014-12-31T23:59:59"),
                new RowCellExtractor(40, Cell.CELL_TYPE_NUMERIC)
        ));
        Iterator<Row> walkingIterator = workbook.getSheetAt(1).rowIterator();
        walkingIterator.next();
        walkingIterator.next();
        walkingIterator.next();
        walkingIterator.next();
        while(walkingIterator.hasNext()){
            Row row = walkingIterator.next();
            subjectLabelExtractor.setRow(row);
            for (TimedValueExtractor extractor : walk5xWeekExtractors){
                ((RowCellExtractor)extractor.getValueExtractor()).setRow(row);
                try {
                    timedValueBuffer.add(extractor.extract());
                }catch (UnknownSubjectLabelException e){
                    // No worries if the subject does not exist
                }catch (ExtractorException e){
                    log.warn(e.getMessage());
                }
            }
        }

        // Extract cycling
        ConstantExtractor cycle1xWeekAttributeLabelExtractor = new ConstantExtractor(AttributeId.cycle1xWeek.name());
        List<TimedValueExtractor> cycle1xWeekExtractors = new ArrayList<>();
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2011-12-31T23:59:59"),
                new RowCellExtractor(5,Cell.CELL_TYPE_NUMERIC)
        ));
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2012-12-31T23:59:59"),
                new RowCellExtractor(16, Cell.CELL_TYPE_NUMERIC)
        ));
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2013-12-31T23:59:59"),
                new RowCellExtractor(27, Cell.CELL_TYPE_NUMERIC)
        ));
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2014-12-31T23:59:59"),
                new RowCellExtractor(38, Cell.CELL_TYPE_NUMERIC)
        ));
        Iterator<Row> cyclingIterator = workbook.getSheetAt(2).rowIterator();
        cyclingIterator.next();
        cyclingIterator.next();
        cyclingIterator.next();
        cyclingIterator.next();
        while(cyclingIterator.hasNext()){
            Row row = cyclingIterator.next();
            subjectLabelExtractor.setRow(row);
            for (TimedValueExtractor extractor : cycle1xWeekExtractors){
                ((RowCellExtractor)extractor.getValueExtractor()).setRow(row);
                try {
                    timedValueBuffer.add(extractor.extract());
                }catch (UnknownSubjectLabelException e){
                    // No worries if the subject does not exist
                }catch (ExtractorException e){
                    log.warn(e.getMessage());
                }
            }
        }

        TimedValueUtils.save(timedValueBuffer);
        return timedValueBuffer.size();
    }

    private Attribute getAttribute(AttributeId attributeId){
        switch (attributeId){
            case walk5xWeek:
               return new Attribute(
                       getProvider(),
                       AttributeId.walk5xWeek.name(),
                       "Walk 5x Week",
                        "% of population who walk for at least 30 minutes, at least 5 x week",
                        Attribute.DataType.numeric
               );
            case cycle1xWeek:
                return new Attribute(
                        getProvider(),
                        AttributeId.cycle1xWeek.name(),
                        "Cycle 1x Week",
                        "% of population who cycle at least 1 x week",
                        Attribute.DataType.numeric
                );
            default:
                return null;
        }
    }
}
