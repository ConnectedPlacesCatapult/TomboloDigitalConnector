package uk.org.tombolo.importer.govuk;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
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
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class LsoaImporter extends AbstractImporter implements Importer {
    private static enum SubjectTypeLabel {lsoa};

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
            case lsoa:
                Datasource datasource = new Datasource(SubjectTypeLabel.lsoa.name(), getProvider(), "LSOA", "Lower Layer Super Output Areas");
                datasource.setRemoteDatafile("https://geoportal.statistics.gov.uk/Docs/Boundaries/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                datasource.setLocalDatafile("govuk/lsoa/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip");
                return datasource;
            default:
                throw new IllegalArgumentException(String.format("Datasource is not valid: %s", datasourceId));
        }
    }

    @Override
    public int importDatasource(Datasource datasource) throws Exception {
        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = getShapefileReaderForDatasource(datasource);
        SubjectType lsoaSubjectType = SubjectTypeUtils.getSubjectTypeByLabel(datasource.getId());
        List<Subject> subjects = convertFeaturesToSubjects(featureReader, lsoaSubjectType);
        SubjectUtils.save(subjects);
        return subjects.size();
    }

    private List<Subject> convertFeaturesToSubjects(FeatureReader<SimpleFeatureType, SimpleFeature> featureReader, SubjectType lsoaSubjectType) throws FactoryException, IOException, TransformException {
        MathTransform crsTransform = makeCrsTransform();

        List<Subject> subjects = new ArrayList<>();
        while (featureReader.hasNext()) {
            SimpleFeature feature = (SimpleFeature) featureReader.next();
            String label = (String) feature.getAttribute("LSOA11CD");
            String name = (String) feature.getAttribute("LSOA11NM");
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            Geometry transformedGeom;
            try {
                transformedGeom = JTS.transform(geom, crsTransform);
                transformedGeom.setSRID(4326); // EPSG:4326
                subjects.add(new Subject(lsoaSubjectType, label, name, transformedGeom));
            } catch (ProjectionException e) {
                System.out.println(String.format("Rejecting %s. You will see this if you have assertions enabled (e.g. " +
                        "you run with `-ea`) as GeoTools runs asserts. See source of LsoaImporter for details on this. " +
                        "To fix this, replace `-ea` with `-ea -da:org.geotools...` in your test VM options (probably in" +
                        "your IDE) to disable assertions in GeoTools.", label));
                // Effectively, GeoTools will run asserts on transforms by converting and then converting back to check
                // the transform occurs within some tolerance (for us, 0.1E-6). Due to some misleading code in GeoTools
                // TransverseMercator.java, the code claims to test against something sensible (0.1E-6 meters) but actually
                // tests against 0.1E-6 in the units of the CRS. Because EPSG:27700 is much bigger (in the thousands) than
                // EPSG:4326 (in the hundreds) we lose some precision — tripping the threshold for many LSOAs.
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

    private FeatureReader<SimpleFeatureType, SimpleFeature> getShapefileReaderForDatasource(Datasource datasource) throws IOException {
        File localFile = downloadUtils.getDatasourceFile(datasource);
        ZipFile zipFile = new ZipFile(localFile);
        Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
        Path tempDirectory = Files.createTempDirectory("shapefile");

        // We copy all of these files because 4 of them are needed for the shapefile to be readable.
        // Copying all of them is simpler than copying a select 4 :)
        while (zipEntries.hasMoreElements()) {
            ZipArchiveEntry entry = zipEntries.nextElement();
            Files.copy(zipFile.getInputStream(entry), Paths.get(tempDirectory.toString(), "/" + entry.getName()), StandardCopyOption.REPLACE_EXISTING);
        }

        ShapefileDataStore store = new ShapefileDataStore(Paths.get(tempDirectory.toString(), "/LSOA_2011_EW_BGC_V2.shp").toUri().toURL());

        DefaultQuery query = new DefaultQuery(store.getTypeNames()[0]);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }
}
