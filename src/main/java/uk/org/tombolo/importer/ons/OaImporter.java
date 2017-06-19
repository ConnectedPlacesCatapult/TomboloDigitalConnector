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
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OaImporter extends AbstractONSImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(OaImporter.class);
    public enum OaType {lsoa, msoa, localAuthority};

    List<String> subjectTypeNames = Arrays.asList("LSOA", "MSOA", "Local Authority");
    private static List<String> subjectTYpeDesc = Arrays.asList(
            "Lower Layer Super Output Areas",
            "Middle Layer Super Output Areas",
            "Local Authority");

    List<String> datafiles = Arrays.asList(
            "http://geoportal.statistics.gov.uk/datasets/da831f80764346889837c72508f046fa_2.geojson",
            "http://geoportal.statistics.gov.uk/datasets/826dc85fb600440889480f4d9dbb1a24_2.geojson", // MSOA
            "http://geoportal.statistics.gov.uk/datasets/3943c2114d764294a7c0079c4020d558_4.geojson" // LA
    );

    public OaImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(OaType.class);
    }

    public static SubjectType getSubjectType(OaType oaType){
        return SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER, oaType.name(), subjectTYpeDesc.get(oaType.ordinal()));
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        OaType datasourceIdObject = OaType.valueOf(datasourceId);
        Datasource datasource = new Datasource(
                        getClass(),
                        datasourceIdObject.name(),
                        getProvider(),
                        subjectTypeNames.get(datasourceIdObject.ordinal()),
                        subjectTYpeDesc.get(datasourceIdObject.ordinal()));
        datasource.addSubjectType(getSubjectType(datasourceIdObject));
        return datasource;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        OaType datasourceId = OaType.valueOf(datasource.getId());
        InputStream inputStream = downloadUtils.fetchJSONStream(new URL(datafiles.get(datasourceId.ordinal())), getProvider().getLabel());
        FeatureIterator<SimpleFeature> featureIterator = new FeatureJSON().streamFeatureCollection(inputStream);

        List<Subject> subjects = new ArrayList<Subject>();
        while(featureIterator.hasNext()) {
            Feature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
            geometry.setSRID(Subject.SRID);

            subjects.add(new Subject(
                    datasource.getUniqueSubjectType(),
                    getFeatureSubjectLabel(feature),
                    getFeatureSubjectName(feature),
                    geometry
            ));
        }

        saveAndClearSubjectBuffer(subjects);
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
