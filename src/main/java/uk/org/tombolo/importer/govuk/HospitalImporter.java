package uk.org.tombolo.importer.govuk;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HospitalImporter extends AbstractImporter implements Importer {
    private static enum SubjectTypeLabel {hospital};

    @Override
    public Provider getProvider() {
        return new Provider(
                "uk.gov.data",
                "data.gov.uk"
        );
    }

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        List<Datasource> datasources = new ArrayList<Datasource>();
        for (SubjectTypeLabel datasourceId : SubjectTypeLabel.values()){
            datasources.add(getDatasource(datasourceId.name()));
        }
        return datasources;
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        SubjectTypeLabel datasourceIdObject = SubjectTypeLabel.valueOf(datasourceId);
        switch (datasourceIdObject) {
            case hospital:
                Datasource datasource = new Datasource(SubjectTypeLabel.hospital.name(), getProvider(), "Hospital", "List of Hospitals in England");
                datasource.setUrl("https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20hospitals%3B");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    public int importDatasource(Datasource datasource) throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
        SubjectType poiType = getSubjectType(SubjectTypeLabel.valueOf(datasource.getId()));
        JSONObject documentObj = downloadUtils.fetchJSON(new URL(datasource.getUrl()));

        List<Map<String, String>> results = (List<Map<String, String>>) documentObj.get("result");

        List<Subject> subjects = results.stream().map(hospitalObj -> {
                String label = hospitalObj.get("organisation_id");
                String name = hospitalObj.get("organisation_name");
                Double longitude = Double.parseDouble(hospitalObj.get("longitude"));
                Double latitude = Double.parseDouble(hospitalObj.get("latitude"));
                Coordinate coordinate = new Coordinate(longitude, latitude);
                Point point = geometryFactory.createPoint(coordinate);
                return new Subject(poiType, label, name, point);
        }).collect(Collectors.toList());

        SubjectUtils.save(subjects);

        return subjects.size();
    }

    @Override
    public void setDownloadUtils(DownloadUtils downloadUtils) {
        this.downloadUtils = downloadUtils;
    }

    private SubjectType getSubjectType(SubjectTypeLabel subjectTypeLabel){
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel.name());
        if (subjectType == null || subjectType.getLabel() == null){
            subjectType = new SubjectType(subjectTypeLabel.name(), "Hospital");
            SubjectTypeUtils.save(subjectType);
        }
        return subjectType;
    }
}
