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
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DownloadUtils;
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
 * Importer for importing the walking and cycling statistics for boroughs.
 */
public class WalkingCyclingBoroughImporter extends AbstractLondonDatastoreImporter {
    Logger log = LoggerFactory.getLogger(WalkingCyclingBoroughImporter.class);

    private enum DatasourceId {
        walkingCyclingBorough(new DatasourceSpec(
                WalkingCyclingBoroughImporter.class,
                "walkingCyclingBorough",
                "Walking and Cycling in London Boroughs",
                "Walking and Cycling in London Boroughs",
                "http://data.london.gov.uk/dataset/walking-and-cycling-borough")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) { this.datasourceSpec = datasourceSpec; }
    }

    public enum AttributeId {walk5xWeek,cycle1xWeek};

    ExcelUtils excelUtils = new ExcelUtils();

    private static final String DATAFILE_SUFFIX = ".xls";
    private static final String DATAFILE
            = "https://files.datapress.com/london/dataset/walking-and-cycling-borough/walking-cycling-borough.xls";

    public WalkingCyclingBoroughImporter(Config config) {
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
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
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        List<Attribute> attributes = new ArrayList<>();
        Arrays.stream(AttributeId.values()).map(attributeId -> getAttribute(attributeId)).forEach(attributes::add);
        return attributes;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType subjectType = OaImporter.getSubjectType(OaImporter.OaType.localAuthority);

        Workbook workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(new URL(DATAFILE), getProvider().getLabel(), DATAFILE_SUFFIX));
        RowCellExtractor subjectLabelExtractor = new RowCellExtractor(0, CellType.STRING);

        // Extract walking
        ConstantExtractor walk5xWeekAttributeLabelExtractor = new ConstantExtractor(AttributeId.walk5xWeek.name());
        List<TimedValueExtractor> walk5xWeekExtractors = new ArrayList<>();
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2011-12-31T23:59:59"),
                new RowCellExtractor(7,CellType.NUMERIC)
        ));
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2012-12-31T23:59:59"),
                new RowCellExtractor(18, CellType.NUMERIC)
        ));
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2013-12-31T23:59:59"),
                new RowCellExtractor(29, CellType.NUMERIC)
        ));
        walk5xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                walk5xWeekAttributeLabelExtractor,
                new ConstantExtractor("2014-12-31T23:59:59"),
                new RowCellExtractor(40, CellType.NUMERIC)
        ));
        Sheet walkSheet = workbook.getSheetAt(1);
        excelUtils.extractAndSaveTimedValues(walkSheet, this, walk5xWeekExtractors);

        // Extract cycling
        ConstantExtractor cycle1xWeekAttributeLabelExtractor = new ConstantExtractor(AttributeId.cycle1xWeek.name());
        List<TimedValueExtractor> cycle1xWeekExtractors = new ArrayList<>();
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2011-12-31T23:59:59"),
                new RowCellExtractor(5, CellType.NUMERIC)
        ));
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2012-12-31T23:59:59"),
                new RowCellExtractor(16, CellType.NUMERIC)
        ));
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2013-12-31T23:59:59"),
                new RowCellExtractor(27, CellType.NUMERIC)
        ));
        cycle1xWeekExtractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                cycle1xWeekAttributeLabelExtractor,
                new ConstantExtractor("2014-12-31T23:59:59"),
                new RowCellExtractor(38, CellType.NUMERIC)
        ));
        Sheet cycleSheet = workbook.getSheetAt(2);
        excelUtils.extractAndSaveTimedValues(cycleSheet, this, cycle1xWeekExtractors);
    }

    private Attribute getAttribute(AttributeId attributeId){
        switch (attributeId){
            case walk5xWeek:
               return new Attribute(
                       getProvider(),
                       AttributeUtils.nameToLabel(AttributeId.walk5xWeek.name()),
                        "% of population who walk for at least 30 minutes, at least 5 x week"
               );
            case cycle1xWeek:
                return new Attribute(
                        getProvider(),
                        AttributeUtils.nameToLabel(AttributeId.cycle1xWeek.name()),
                        "% of population who cycle at least 1 x week"
                );
            default:
                return null;
        }
    }
}
