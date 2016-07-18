package uk.org.tombolo.importer.govuk;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class OaImporter extends AbstractImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(OaImporter.class);
    private static enum SubjectTypeLabel {lsoa, msoa};

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
        Datasource datasource;
        switch (datasourceIdObject) {
            case lsoa:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "LSOA", "Lower Layer Super Output Areas");
                datasource.setRemoteDatafile("https://geoportal.statistics.gov.uk/Docs/Boundaries/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                datasource.setLocalDatafile("govuk/lsoa/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                return datasource;
            case msoa:
                datasource = new Datasource(datasourceIdObject.name(), getProvider(), "MSOA", "Middle Layer Super Output Areas");
                datasource.setRemoteDatafile("https://geoportal.statistics.gov.uk/Docs/Boundaries/Middle_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                datasource.setLocalDatafile("govuk/msoa/Middle_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    public int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate(datasource.getId(), datasource.getDescription());

        ShapefileDataStore store = getShapefileDataStoreForDatasource(datasource, subjectType);
        FeatureReader featureReader = getFeatureReader(store);

        List<Subject> subjects = convertFeaturesToSubjects(featureReader, subjectType);
        SubjectUtils.save(subjects);

        featureReader.close();
        store.dispose();
        
        return subjects.size();
    }

    private FeatureReader getFeatureReader(ShapefileDataStore store) throws IOException {
        DefaultQuery query = new DefaultQuery(store.getTypeNames()[0]);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    private List<Subject> convertFeaturesToSubjects(FeatureReader<SimpleFeatureType, SimpleFeature> featureReader, SubjectType subjectType) throws FactoryException, IOException, TransformException {
        MathTransform crsTransform = makeCrsTransform();

        List<Subject> subjects = new ArrayList<>();
        while (featureReader.hasNext()) {
            SimpleFeature feature = featureReader.next();
            String label = (String) feature.getAttribute(fieldNameForSubjectType(subjectType, "11CD"));
            String name = (String) feature.getAttribute(fieldNameForSubjectType(subjectType, "11NM"));
            Geometry geom = (Geometry) feature.getDefaultGeometry();

            try {
                Geometry transformedGeom = JTS.transform(geom, crsTransform);
                transformedGeom.setSRID(4326); // EPSG:4326
                subjects.add(new Subject(subjectType, label, name, transformedGeom));
            } catch (ProjectionException e) {
                log.warn("Rejecting {}. You will see this if you have assertions enabled (e.g. " +
                        "you run with `-ea`) as GeoTools runs asserts. See source of OaImporter for details on this. " +
                        "To fix this, replace `-ea` with `-ea -da:org.geotools...` in your test VM options (probably in" +
                        "your IDE) to disable assertions in GeoTools.", label);
                // Effectively, GeoTools will run asserts on transforms by converting and then converting back to check
                // the transform occurs within some tolerance (for us, 0.1E-6). Due to some misleading code in GeoTools
                // TransverseMercator.java, the code claims to test against something sensible (0.1E-6 meters) but actually
                // tests against 0.1E-6 in the units of the CRS. Because EPSG:27700 is much bigger (in the thousands) than
                // EPSG:4326 (in the hundreds) we lose some precision — tripping the threshold for many OAs.
            }
        }
        return subjects;
    }

    private MathTransform makeCrsTransform() throws FactoryException {
        CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:27700");
        // The 'true' here means longitude first. Don't know why GeoTools puts lat first by default for this CRS
        // There's a `.prj` file with this dataset, but it seems to result in transforms being ~10m off longitude-wise, so we ignore it
        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:4326", true);

        return CRS.findMathTransform(sourceCrs, targetCrs);
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
