package uk.org.tombolo.importer.ons;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.AbstractImporter;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OaImporter extends AbstractImporter {
    private static Logger log = LoggerFactory.getLogger(OaImporter.class);

    public enum OaType {
        lsoa(new DatasourceSpec(OaImporter.class,"lsoa","LSOA","Lower Layer Super Output Areas",null),
                "https://raw.githubusercontent.com/FutureCitiesCatapult/TomboloOpenData/master/UK_2011_Census_Boundaries__LSOA.geojson"),
        msoa(new DatasourceSpec(OaImporter.class, "msoa", "MSOA", "Middle Layer Super Output Areas", null),
                "https://raw.githubusercontent.com/FutureCitiesCatapult/TomboloOpenData/master/UK_2011_Census_Boundaries__MSOA.geojson"),
        localAuthority(new DatasourceSpec(OaImporter.class, "localAuthority", "Local Authority", "Local Authority", null),
                "https://raw.githubusercontent.com/FutureCitiesCatapult/TomboloOpenData/master/UK_2011_Census_Boundaries__Local_Authority.geojson");


        public DatasourceSpec datasourceSpec;
        private String datafile;
        OaType(DatasourceSpec datasourceSpec, String datafile) {
            this.datasourceSpec = datasourceSpec;
            this.datafile = datafile;
        }
    }

    public OaImporter(){
        datasourceIds = stringsFromEnumeration(OaType.class);
    }

    public static SubjectType getSubjectType(OaType oaType){
        return new SubjectType(AbstractONSImporter.PROVIDER,
                                                    oaType.name(), oaType.datasourceSpec.getDescription());
    }

    @Override
    public Provider getProvider() {
        return AbstractONSImporter.PROVIDER;
    }

    @Override
    public List<SubjectType> getSubjectTypes(String datasourceId) {
        return Collections.singletonList(getSubjectType(OaType.valueOf(datasourceId)));
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return OaType.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope,  List<String> datasourceLocation) throws Exception {
        InputStream inputStream = downloadUtils.fetchInputStream(new URL(OaType.valueOf(datasource.getDatasourceSpec().getId()).datafile), getProvider().getLabel(), ".json");
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
