package uk.org.tombolo.importer.nhschoices;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HealthOrganisationImporter extends AbstractImporter implements Importer {
    private Logger log = LoggerFactory.getLogger(HealthOrganisationImporter.class);
    private enum DatasourceId {hospital, clinic, gpSurgeries};

    public HealthOrganisationImporter() {
        super();
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public Provider getProvider() {
        return new Provider(
                "uk.nhs",
                "NHS Choices"
        );
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DatasourceId datasourceIdObject = DatasourceId.valueOf(datasourceId);
        switch (datasourceIdObject) {
            case hospital:
                return makeDatasource(
                        datasourceIdObject.name(), "Hospital", "List of Hospitals in England", "https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20hospitals%3B");
            case clinic:
                return makeDatasource(
                        datasourceIdObject.name(), "Clinic", "List of Clinics in England", "https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20clinics%3B");
            case gpSurgeries:
                log.warn("GP Surgeries dataset known to have erroneous data, for an example see 'Dr Rushton & Partne.478378295898438' or watch import logs");
                return makeDatasource(
                        datasourceIdObject.name(), "GP Surgeries", "List of GP Surgeries in England", "https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20gp_surgeries%3B");
            default:
                return null;
        }
    }

    private Datasource makeDatasource(String id, String name, String description, String url) {
        Datasource datasource = new Datasource(getClass(), id, getProvider(), name, description);
        datasource.addSubjectType(new SubjectType(datasource.getId(), datasource.getName()));
        datasource.setUrl(url);
        return datasource;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
        JSONObject documentObj = downloadUtils.fetchJSON(new URL(datasource.getUrl()), getProvider().getLabel());

        List<Map<String, String>> results = (List<Map<String, String>>) documentObj.get("result");

        List<Subject> subjects = results.stream().map(healthOrgObj -> {
            String label = healthOrgObj.get("organisation_id");
            String name = healthOrgObj.get("organisation_name");
            Point point = null;
            try {
                Double longitude = Double.parseDouble(healthOrgObj.get("longitude"));
                Double latitude = Double.parseDouble(healthOrgObj.get("latitude"));
                Coordinate coordinate = new Coordinate(longitude, latitude);
                point = geometryFactory.createPoint(coordinate);

                // FIXME: Add fixed values

            } catch (Exception e) {
                // If we have any trouble with the geometry, e.g. the figures are blank or invalid,
                // we use the null geometry prepopulated in the `point` variable, and log.
                log.warn("Health organisation {} ({}) has no valid geometry", name, label);
            }
            return new Subject(datasource.getUniqueSubjectType(), label, name, point);

        }).collect(Collectors.toList());

        saveAndClearSubjectBuffer(subjects);
    }
}
