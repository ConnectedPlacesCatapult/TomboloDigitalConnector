package uk.org.tombolo.importer.govuk;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class HospitalImporter extends AbstractImporter implements Importer {
    private static enum SubjectTypeLabel {Hospital};

    @Override
    public Provider getProvider() {
        return new Provider(
                "uk.gov.data",
                "data.gov.uk"
        );
    }

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return null;
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        SubjectTypeLabel datasourceIdObject = SubjectTypeLabel.valueOf(datasourceId);
        switch (datasourceIdObject) {
            case hospital:
                Datasource datasource = new Datasource(SubjectTypeLabel.hospital.name(), getProvider(), "Hospital", "List of Hospitals in England");
                datasource.setUrl("https://data.gov.uk/data/api/service/health/hospitals/all_hospitals");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    public int importDatasource(Datasource datasource) throws Exception {
        SubjectType poiType = getSubjectType(SubjectTypeLabel.valueOf(datasource.getName()));
        JSONObject documentObj = downloadUtils.fetchJSON(new URL(datasource.getUrl()));

        JSONArray results = (JSONArray) documentObj.get("result");

        List<Subject> subjects = (List<Subject>) results.stream().map(resultObj -> {
            JSONObject hospitalObj = (JSONObject) resultObj;
            String label = (String) hospitalObj.get("organisation_id");
            String name = (String) hospitalObj.get("organisation_name");
            return new Subject(poiType, label, name, null);
        }).collect(Collectors.toList());

        SubjectUtils.save(subjects);

        return subjects.size();
    }

    @Override
    public void setDownloadUtils(DownloadUtils downloadUtils) {
        this.downloadUtils = downloadUtils;
    }

    @Override
    public void setTimedValueUtils(TimedValueUtils timedValueUtils) {
        this.timedValueUtils = timedValueUtils;
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
