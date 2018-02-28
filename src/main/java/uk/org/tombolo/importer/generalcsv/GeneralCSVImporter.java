package uk.org.tombolo.importer.generalcsv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.GeneralImporter;
import uk.org.tombolo.importer.utils.CoordinateUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * General importer for CSV files.
 */
public class GeneralCSVImporter extends GeneralImporter {
    static Logger log = LoggerFactory.getLogger(GeneralCSVImporter.class);

    private List csvRecords;
    private DatasourceSpec datasourceSpec;

    public GeneralCSVImporter(Config config) {
        super(config);

        datasourceSpec = new DatasourceSpec(GeneralCSVImporter.class,
                "datasource" + config.getProvider().substring(0, 1).toUpperCase() + config.getProvider().substring(1),
                "", "", "");

        datasourceIds = Arrays.asList(datasourceSpec.getId());
    }

    @Override
    public Provider getProvider() {
        return new Provider(config.getProvider(), "");
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        setupUtils();
        return datasourceSpec;
    }

    @Override
    public List<SubjectType> getSubjectTypes(String dataSourceID) {
        if ("yes".equalsIgnoreCase(config.getExistingSubject())) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.getSubjectType());
    }

    @Override
    public List<Attribute> getFixedValueAttributes(String datasourceID) {
        List<Attribute> attributes = new ArrayList<>();

        CSVRecord attributeHeader = (CSVRecord) csvRecords.get(0);

        List<Integer> attributeIndexes;
        attributeIndexes = IntStream.rangeClosed(1, attributeHeader.size() - 1).boxed().collect(Collectors.toList());

        if (!config.getGeographyProjection().equals("")) {
            attributeIndexes.remove(config.getGeographyXIndex());
            attributeIndexes.remove(config.getGeographyYIndex());
        }

        for (int index : attributeIndexes) {
            String attrString = attributeHeader.get(index);
            attributes.add(new Attribute(
                    getProvider(),
                    attrString,
                    attrString
            ));
        }

        return attributes;
    }

    protected void setupUtils() throws Exception {
        CSVFormat format = CSVFormat.DEFAULT;
        String fileLocation = config.getFileLocation();
        URL url;
        try {
            url = new URL(fileLocation);
        } catch (MalformedURLException e) {
            File file;
            if (!(file = new File(fileLocation)).exists()) {
                log.error("File does not exist: ", fileLocation);
            }
            url = file.toURI().toURL();
        }

        InputStreamReader isr = new InputStreamReader(
                downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".csv"));
        CSVParser csvFileParser = new CSVParser(isr, format);
        csvRecords = csvFileParser.getRecords();
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        int subjectIDIdx = config.getSubjectIDIndex();
        List<FixedValue> fixedValues = new ArrayList<>();
        List<Subject> subjects = new ArrayList<>();
        Iterator<CSVRecord> csvRecordIterator = csvRecords.iterator();
        CSVRecord header = csvRecordIterator.next();
        Subject subject;

        boolean newSubject = "no".equalsIgnoreCase(config.getExistingSubject());

        while (csvRecordIterator.hasNext()) {
            CSVRecord record = csvRecordIterator.next();
            if (newSubject) {
                subject = new Subject(
                        datasource.getUniqueSubjectType(),
                        record.get(config.getSubjectIDIndex()),
                        "",
                        getShape(record)
                );
                subjects.add(subject);

            } else {
                subject = SubjectUtils.getSubjectByTypeAndLabel(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                config.getSubjectType().getProvider().getLabel(),
                                config.getSubjectType().getLabel()
                        ),
                        record.get(subjectIDIdx)
                );
            }

            int attributeIndex = 1;
            for (Attribute attribute : datasource.getFixedValueAttributes()) {
                fixedValues.add(new FixedValue(
                        subject,
                        attribute,
                        record.get(attributeIndex++)));
            }
        }

        if (newSubject) saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }

    private Geometry getShape(CSVRecord record) {
        if (!config.getGeographyProjection().equals("") &&
                config.getGeographyXIndex() != -1 &&
                config.getGeographyYIndex() != -1) {

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);

            Coordinate coordinate = null;
            Double x = Double.parseDouble(record.get(config.getGeographyXIndex()));
            Double y = Double.parseDouble(record.get(config.getGeographyYIndex()));
            if (config.getGeographyProjection().equals(CoordinateUtils.WGS84CRS)) {

                coordinate = new Coordinate(x, y);
            } else {
                try {
                    coordinate = CoordinateUtils.eastNorthToLatLong(x, y, config.getGeographyProjection(), CoordinateUtils.WGS84CRS);
                } catch (Exception e) {
                    log.warn("Coordinates will not be considered: " + e.getMessage());
                    return null;
                }
            }
            return geometryFactory.createPoint(coordinate);
        }

        return null;
    }
}
