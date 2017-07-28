package uk.org.tombolo.importer.lac;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.utils.JSONReader;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * London Air Quality Importer
 */
public class LAQNImporter extends AbstractImporter{

    private static final String LAQN_PROVIDER_LABEL = "erg.kcl.ac.uk";
    private static final String LAQN_PROVIDER_NAME = "Environmental Research Group Kings College London";
    private static final String LAQN_SUBJECT_TYPE_LABEL = "airQualityControl";
    private static final String LAQN_SUBJECT_TYPE_DESC = "Quantity of gases in air by Kings College London";
    private String dataSourceURL = "http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/";
    private static DataSourceID dataSourceID;
    private JSONReader reader;
    private ArrayList<LinkedHashMap<String, List<String>>> flatJson;
    private LAQNConfig laqnConfig;

    public LAQNImporter(Config config) throws IOException {
        super(config);

        if (config.getFileLocation() != null && !config.getFileLocation().trim().isEmpty()) {
            laqnConfig = config(config.getFileLocation());
        }

        dataSourceID = new DataSourceID(LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_DESC,
                importerURL(), "");
        datasourceIds = Arrays.asList(dataSourceID.getLabel());
        flatJson = readData();
    }

    @Override
    public Provider getProvider() {
        return new Provider(LAQN_PROVIDER_LABEL, LAQN_PROVIDER_NAME);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {

        Datasource datasource = new Datasource(getClass(), LAQN_SUBJECT_TYPE_LABEL, getProvider(),
                LAQN_SUBJECT_TYPE_LABEL, LAQN_SUBJECT_TYPE_DESC);

        datasource.addAllFixedValueAttributes(getFixedAttributes());
        datasource.addAllSubjectTypes(Arrays.asList(getSubjectType()));


        return datasource;

    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

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
        saveAndClearFixedValueBuffer(getFixedValue(subjects));
    }


    private ArrayList<Attribute> getFixedAttributes() throws IOException {

        return reader.allUniquekeys().stream().map(attr -> new Attribute(
                getProvider(),
                attr.substring(1),
                attr.substring(1),
                attr.substring(1),
                Attribute.DataType.string
        )).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<LinkedHashMap<String, List<String>>> readData() throws IOException {
        reader = new JSONReader(dataSourceID.getUrl());
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


    private ArrayList<FixedValue> getFixedValue(List<Subject> subjects) throws IOException {
        ArrayList<FixedValue> fixedValues = new ArrayList<>();
//        ArrayList<Subject> subjects = getSubjects(getSubjectType());
        ArrayList<Attribute> attributes = getFixedAttributes();

//        System.out.println(subjects.size() + " these are subjects");
//        System.out.println(flatJson.size() + " this is data");
//        System.out.println(flatJson.get(121).get("@SiteCode"));

        IntStream.range(0, flatJson.size()).forEach(i -> {
            Subject subject = subjects.get(i);
            attributes.forEach(attribute -> {
                List<String> values = flatJson.get(i).get("@" + attribute.getLabel());

//                if (values.size() > 1) {
//                    values.stream().map(value -> new FixedValue(subject, attribute, value))
//                            .forEach(fixedValues::add);
//                } else
                    fixedValues.add(new FixedValue(subject, attribute, values.toString()));
            });
        });
//
        for (FixedValue fv : fixedValues) {
            System.out.println(fv.getId().getSubject().getLabel() +
            " " + fv.getId().getSubject().getName() + " " +
            fv.getId().getAttribute().getLabel() + " " +
            fv.getValue());
        }

        return fixedValues;
    }


    private Geometry shape(String latitude, String longitude) {
        return new GeometryFactory()
                .createPoint(new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude)));
    }

    private LAQNConfig config(String fileName) throws IOException {
        LAQNConfig config = new LAQNConfig();

        Properties properties = new Properties();
        InputStream stream = getClass().getResourceAsStream(fileName);


        if (stream != null) properties.load(stream);
        else throw new FileNotFoundException(fileName);

        config.setYear(properties.getProperty("year"));
        config.setArea(properties.getProperty("area"));

        return config;
    }

    private String importerURL() {

        if (dataSourceURL.contains("GroupName")) return dataSourceURL;

        dataSourceURL = laqnConfig != null && laqnConfig.getArea() != null && !laqnConfig.getArea().trim().isEmpty() ?
                            dataSourceURL + "GroupName=" + laqnConfig.getArea().trim() + "/" :
                            dataSourceURL + "GroupName=London" + "/";

        dataSourceURL = laqnConfig != null && laqnConfig.getYear() != null && !laqnConfig.getYear().trim().isEmpty() ?
                            dataSourceURL + "Year=" + laqnConfig.getYear().trim() + "/json" : dataSourceURL + "json";

        return dataSourceURL;
    }

}
