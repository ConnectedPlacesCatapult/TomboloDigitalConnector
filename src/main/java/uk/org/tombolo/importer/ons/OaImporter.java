package uk.org.tombolo.importer.ons;

import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ShapefileImporter;
import uk.org.tombolo.importer.ShapefileUtils;
import uk.org.tombolo.importer.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class OaImporter extends AbstractONSImporter implements Importer, ShapefileImporter {
    private static Logger log = LoggerFactory.getLogger(OaImporter.class);
    private static enum SubjectTypeLabel {lsoa, msoa};

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
        Datasource datasource;
        switch (datasourceIdObject) {
            case lsoa:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "LSOA", "Lower Layer Super Output Areas");
                datasource.setRemoteDatafile("https://geoportal.statistics.gov.uk/Docs/Boundaries/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                datasource.setLocalDatafile("lsoa/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                return datasource;
            case msoa:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "MSOA", "Middle Layer Super Output Areas");
                datasource.setRemoteDatafile("https://geoportal.statistics.gov.uk/Docs/Boundaries/Middle_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                datasource.setLocalDatafile("msoa/Middle_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate(datasource.getId(), datasource.getDescription());

        ShapefileDataStore store = getShapefileDataStoreForDatasource(datasource, subjectType);
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);

        List<Subject> subjects = ShapefileUtils.convertFeaturesToSubjects(featureReader, subjectType, this);
        SubjectUtils.save(subjects);

        featureReader.close();
        store.dispose();
        
        return subjects.size();
    }

    @Override
    public String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType) {
        return (String) feature.getAttribute(fieldNameForSubjectType(subjectType, "11CD"));
    }

    @Override
    public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
        return (String) feature.getAttribute(fieldNameForSubjectType(subjectType, "11NM"));
    }

    @Override
    public String getEncoding() {
        return "EPSG:27700";
    }

    private ShapefileDataStore getShapefileDataStoreForDatasource(Datasource datasource, SubjectType subjectType) throws IOException {
        File localFile = downloadUtils.getDatasourceFile(datasource);
        Path tempDirectory = ZipUtils.unzipToTemporaryDirectory(localFile);
        return new ShapefileDataStore(Paths.get(tempDirectory.toString(), "/"  + shapefileNameForDatasource(subjectType)).toUri().toURL());
    }

    private String shapefileNameForDatasource(SubjectType subjectType) {
        return subjectType.getLabel().toUpperCase() + "_2011_EW_BGC_V2.shp";
    }

    private String fieldNameForSubjectType(SubjectType subjectType, String field) {
        return subjectType.getLabel().toUpperCase() + field;
    }
}
