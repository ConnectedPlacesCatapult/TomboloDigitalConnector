package uk.org.tombolo.importer.lac;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.JSONReader;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * London Air Quality Importer
 */
public class LAQNImporter extends AbstractImporter implements Importer{

    private static final String LAQN_PROVIDER_LABEL = "erg.kcl.ac.uk";
    private static final String LAQN_PROVIDER_NAME = "Environmental Research Group Kings College London";
    private static final String LAQN_SUBJECT_TYPE_LABEL = "airQualityControl";
    private static final String LAQN_SUBJECT_TYPE_DESC = "Quantity of gases in air by Kings College London";
    private static final ArrayList<String> UNIQUE_TAGS = new ArrayList<>(Arrays.asList("@SiteCode",
            "@SiteName", "@SiteType", "@Latitude", "@Longitude", "@LatitudeWGS84", "@LongitudeWGS84",
            "@SiteLink", "@DataOwner", "@DataManager"));
    private String dataSourceURL = "http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/";
    private static DataSourceID dataSourceID;
    private JSONReader reader;
    private int attributeSize;
    private ArrayList<LinkedHashMap<String, List<String>>> flatJson;
    private Config config;

    public LAQNImporter(Config config) throws Exception {
        super(config);

        this.config = config;

        dataSourceID = new DataSourceID(LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_DESC,
                dataSourceURL, "");
        datasourceIds = Arrays.asList(dataSourceID.getLabel());

    }

    public int getAttributeSize() {
        return attributeSize;
    }

    private void setAttributeSize(int attributeSize) {
        this.attributeSize = attributeSize;
    }

    @Override
    public Provider getProvider() {
        return new Provider(LAQN_PROVIDER_LABEL, LAQN_PROVIDER_NAME);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {

        Datasource datasource = new Datasource(getClass(), LAQN_SUBJECT_TYPE_LABEL, getProvider(),
                LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_DESC);
        datasource.addAllSubjectTypes(Arrays.asList(getSubjectType()));
        return datasource;

    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

        if (config.getFileLocation() != null && !config.getFileLocation().trim().isEmpty()) {
            properties = config(config.getFileLocation());
            if (properties != null && !properties.isEmpty()) {
                geographyScope = Collections.singletonList(properties.getProperty("area"));
                temporalScope = Collections.singletonList(properties.getProperty("year"));
            }
        }


        flatJson = readData(importerURL(
                !geographyScope.isEmpty() ? geographyScope.get(0) : "",
                !temporalScope.isEmpty() ? temporalScope.get(0) : ""
        ));

        ArrayList<Attribute> attr = getAttributes();
        setAttributeSize(attr.size());
        AttributeUtils.save(attr);


        SubjectType subjectType = SubjectTypeUtils.getOrCreate(
                datasource.getUniqueSubjectType().getProvider(),
                datasource.getUniqueSubjectType().getLabel(),
                datasource.getUniqueSubjectType().getName()
        );

        saveAndClearSubjectBuffer(getSubjects(subjectType));

        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        ArrayList<Attribute> attributes = getAttributes().stream()
                                        .map(attribute -> AttributeUtils.getByProviderAndLabel(getProvider().getLabel(),
                                        attribute.getLabel())).collect(Collectors.toCollection(ArrayList::new));

        saveAndClearFixedValueBuffer(getFixedValue(subjects, attributes));
        saveAndClearTimedValueBuffer(getTimedValue(subjects, attributes));
    }


    private ArrayList<Attribute> getAttributes() throws IOException {

        ArrayList<Attribute> attributes = new ArrayList<>();

        ArrayList<String> keepTrack = new ArrayList<>();
        flatJson.forEach(data -> {
            List<String> speciesCode = data.get("@SpeciesCode");
            List<String> objectiveName = data.get("@ObjectiveName");
            List<String> speciesDescription = data.get("@SpeciesDescription");
            IntStream.range(0, speciesCode.size()).forEachOrdered(i -> {
                String attrlabel = speciesCode.get(i) + " " +
                        objectiveName.get(i).substring(0, objectiveName.get(i).length() < 25 ?
                                objectiveName.get(i).length() : 24);
                if (!keepTrack.contains(attrlabel)) {

                    attributes.add(new Attribute(getProvider(), attrlabel, speciesDescription.get(i),
                            objectiveName.get(i), Attribute.DataType.string));
                    keepTrack.add(attrlabel);
                }
            });
        });

        reader.allUniquekeys().stream().map(attr -> new Attribute(
                    getProvider(),
                    attr.substring(1),
                    attr.substring(1),
                    "Unique key",
                    Attribute.DataType.string
        )).forEach(attributes::add);

        return attributes;
    }

    private ArrayList<LinkedHashMap<String, List<String>>> readData(String url) throws IOException {
        reader = new JSONReader(url);
        return reader.getData();
    }

    private SubjectType getSubjectType() {
        return new SubjectType(getProvider(), LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_DESC);
    }

    private ArrayList<Subject> getSubjects(SubjectType subjectType) {
        return flatJson.stream().map(sections -> new Subject(
                subjectType,
                sections.get("@SiteCode").get(0),
                sections.get("@SiteName").get(0),
                shape(sections.get("@Latitude").get(0), sections.get("@Longitude").get(0))
        )).collect(Collectors.toCollection(ArrayList::new));

    }

    private ArrayList<FixedValue> getFixedValue(List<Subject> subjects, ArrayList<Attribute> attributes) throws IOException {
        ArrayList<FixedValue> fixedValues = new ArrayList<>();

        for (int i = 0; i < flatJson.size(); i++) {
            Subject subject = subjects.get(i);
            int j = 0;
            for (Attribute attribute : attributes) {

                if (attribute.getLabel().startsWith(subject.getLabel())) {
                    List<String> values = flatJson.get(i).get("@Year");
                    fixedValues.add(new FixedValue(subject, attribute, values.get(j)));
                    j++;
                }

                if (UNIQUE_TAGS.contains("@"+attribute.getLabel())) {
                    fixedValues.add(new FixedValue(subject, attribute, flatJson.get(i).get("@"+attribute.getLabel()).get(0)));
                }
            }
        }

        return fixedValues;
    }

    private ArrayList<TimedValue> getTimedValue(List<Subject> subjects, ArrayList<Attribute> attributes) throws InterruptedException {
        ArrayList<TimedValue> timedValues = new ArrayList<>();

        IntStream.range(0, flatJson.size()).forEachOrdered(i -> {
            Subject subject = subjects.get(i);
            IntStream.range(0, flatJson.get(i).get("@SpeciesCode").size()).forEachOrdered(j -> {
                for (Attribute attribute : attributes) {
                    if (attribute.getLabel().startsWith(flatJson.get(i).get("@SpeciesCode").get(j)) &&
                            attribute.getDescription().equalsIgnoreCase(flatJson.get(i).get("@ObjectiveName").get(j))) {

                        timedValues.add(new TimedValue(subject, attribute, time(),
                                Double.parseDouble(flatJson.get(i).get("@Value").get(j))));
                        break;
                    }
                }
            });

        });

        return timedValues;
    }

    private LocalDateTime time() {
        return LocalDateTime.now();
    }


    private Geometry shape(String latitude, String longitude) {
        return new GeometryFactory()
                .createPoint(new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude)));
    }

    private Properties config(String fileName) throws Exception {

        Properties properties = new Properties();
        InputStream stream = getClass().getResourceAsStream(fileName);


        if (stream != null) properties.load(stream);
        else throw new FileNotFoundException(fileName);

        configure(properties);

        return getConfiguration();
    }

    private String importerURL(String area, String year) {

        if (dataSourceURL.contains("GroupName")) return dataSourceURL;

        dataSourceURL = area != null && !area.trim().isEmpty() ?
                            dataSourceURL + "GroupName=" + area.trim() + "/" :
                            dataSourceURL + "GroupName=London" + "/";

        dataSourceURL = year != null && !year.isEmpty() ?
                            dataSourceURL + "Year=" + year.trim() + "/json" : dataSourceURL + "json";

        return dataSourceURL;
    }

}
