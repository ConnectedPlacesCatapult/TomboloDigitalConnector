package uk.org.tombolo.importer.lac;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.*;
import uk.org.tombolo.importer.utils.JSONReader;

import java.io.*;
import java.net.URL;
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

    public LAQNImporter(Config config) throws Exception {
        super(config);

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
        flatJson.forEach(data -> IntStream.range(0, data.get("@SpeciesCode").size()).forEachOrdered(i -> {
            String attrlabel = data.get("@SpeciesCode").get(i) + " " +
                    data.get("@ObjectiveName").get(i).substring(0, data.get("@ObjectiveName").get(i).length() < 25 ?
                            data.get("@ObjectiveName").get(i).length() : 24);
            if (!keepTrack.contains(attrlabel)) {

                attributes.add(new Attribute(getProvider(), attrlabel, data.get("@SpeciesDescription").get(i),
                        data.get("@ObjectiveName").get(i), Attribute.DataType.string));
                keepTrack.add(attrlabel);
            }
        }));

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

        reader = new JSONReader(downloadUtils.fetchJSONStream(new URL(url), "uk.lac"));
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

        IntStream.range(0, flatJson.size()).forEachOrdered(i -> {
            Subject subject = subjects.get(i);
            for (Attribute attribute : attributes) {

                if (UNIQUE_TAGS.contains("@"+attribute.getLabel())) {
                    fixedValues.add(new FixedValue(subject, attribute, flatJson.get(i).get("@"+attribute.getLabel()).get(0)));
                }
            }
        });

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

                        timedValues.add(new TimedValue(subject, attribute,
                                time(flatJson.get(i).get("@Year").get(j)),
                                Double.parseDouble(flatJson.get(i).get("@Value").get(j))));
                        break;
                    }
                }
            });

        });

        return timedValues;
    }

    private LocalDateTime time(String time) {
        return TimedValueUtils.parseTimestampString(time);
    }


    private Geometry shape(String latitude, String longitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
        return geometryFactory
                .createPoint(new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude)));
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
