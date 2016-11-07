package uk.org.tombolo.importer.ons;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.Importer;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class OaImporter extends AbstractONSImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(OaImporter.class);
    private static enum SubjectTypeLabel {lsoa, msoa, localAuthority};

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return datasourcesFromEnumeration(SubjectTypeLabel.class);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        SubjectTypeLabel datasourceIdObject = SubjectTypeLabel.valueOf(datasourceId);
        Datasource datasource;
        switch (datasourceIdObject) {
            case lsoa:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "LSOA", "Lower Layer Super Output Areas");
                datasource.setRemoteDatafile("http://geoportal.statistics.gov.uk/datasets/da831f80764346889837c72508f046fa_2.geojson");
                datasource.setLocalDatafile("lsoa/Lower_layer_Super_Output_Areas_December_2011_Generalised_Clipped__Boundaries_in_England_and_Wales.geojson");
                return datasource;
            case msoa:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "MSOA", "Middle Layer Super Output Areas");
                datasource.setRemoteDatafile("http://geoportal.statistics.gov.uk/datasets/826dc85fb600440889480f4d9dbb1a24_2.geojson");
                datasource.setLocalDatafile("msoa/Middle_Layer_Super_Output_Areas_December_2011_Generalised_Clipped_Boundaries_in_England_and_Wales.geojson");
                return datasource;
            case localAuthority:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "Local Authority", "Local Authority");
                datasource.setUrl("http://geoportal.statistics.gov.uk/datasets/3943c2114d764294a7c0079c4020d558_4");
                datasource.setRemoteDatafile("http://geoportal.statistics.gov.uk/datasets/3943c2114d764294a7c0079c4020d558_4.geojson");
                datasource.setLocalDatafile("localAuthority/Local_Authority_Districts_December_2011_Generalised_Clipped_Boundaries_in_Great_Britain.geojson");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate(datasource.getId(), datasource.getDescription());

        InputStream inputStream = downloadUtils.fetchJSONStream(new URL(datasource.getRemoteDatafile()));
        FeatureIterator<SimpleFeature> featureIterator = new FeatureJSON().streamFeatureCollection(inputStream);

        List<Subject> subjects = new ArrayList<Subject>();
        while(featureIterator.hasNext()) {
            Feature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
            geometry.setSRID(Subject.SRID);

            subjects.add(new Subject(
                    subjectType,
                    getFeatureSubjectLabel(feature),
                    getFeatureSubjectName(feature),
                    geometry
            ));
        }

        SubjectUtils.save(subjects);

        return subjects.size();
    }

    private String getFeatureSubjectLabel(Feature feature) {
        return (String) feature.getProperties().stream().filter(
                property -> property.getName().toString().toUpperCase().endsWith("CD")
        ).findFirst().get().getValue();
    }

    private String getFeatureSubjectName(Feature feature) {
        return (String) feature.getProperties().stream().filter(property ->
                property.getName().toString().toUpperCase().endsWith("NM")
        ).findFirst().get().getValue();
    }
}
