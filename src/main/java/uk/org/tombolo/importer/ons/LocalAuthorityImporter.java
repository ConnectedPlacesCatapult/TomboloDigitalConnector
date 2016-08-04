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

public final class LocalAuthorityImporter extends AbstractONSImporter implements Importer, ShapefileImporter {
    private static Logger log = LoggerFactory.getLogger(LocalAuthorityImporter.class);
    private static enum SubjectTypeLabel {localAuthority};

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
            case localAuthority:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "Local Authority", "Local Authority");
                datasource.setRemoteDatafile("https://data.gov.uk/dataset/county-and-unitary-authorities-ew-2012-boundaries-full-extent/datapackage.zip");
                datasource.setLocalDatafile("localAuthority/datapackage.zip");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate(datasource.getId(), datasource.getDescription());

        ShapefileDataStore store = getShapefileDataStoreForDatasource(datasource);
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);

        List<Subject> subjects = ShapefileUtils.convertFeaturesToSubjects(featureReader, subjectType, this);
        SubjectUtils.save(subjects);

        featureReader.close();
        store.dispose();
        
        return subjects.size();
    }

    @Override
    public String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType) {
        return (String) feature.getAttribute("CTYUA12CD");
    }

    @Override
    public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
        return (String) feature.getAttribute("CTYUA12NM");
    }

    private ShapefileDataStore getShapefileDataStoreForDatasource(Datasource datasource) throws IOException {
        File outerZipFile = downloadUtils.getDatasourceFile(datasource);
        Path outerZipContentsPath = ZipUtils.unzipToTemporaryDirectory(outerZipFile);
        File innerZipFile = new File(Paths.get(outerZipContentsPath.toString(), "/data/County_and_unitary_authorities_(E+W)_2012_Boundaries_(Full_Extent).zip").toString());
        Path innerZipContentsPath = ZipUtils.unzipToTemporaryDirectory(innerZipFile);

        return new ShapefileDataStore(Paths.get(innerZipContentsPath.toString(), "/CTYUA_DEC_2012_EW_BFE.shp").toUri().toURL());
    }
}
