package uk.org.tombolo.importer.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
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
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.GeotoolsDataStoreImporter;
import uk.org.tombolo.importer.Importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class GeotoolsDataStoreUtils extends AbstractImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(GeotoolsDataStoreUtils.class);

    public static FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(DataStore store, String typeName) throws IOException {
        DefaultQuery query = new DefaultQuery(typeName);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    private static MathTransform makeCrsTransform(String inputFormat) throws FactoryException {
        CoordinateReferenceSystem sourceCrs = CRS.decode(inputFormat);
        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:"+Subject.SRID, true);

        return CRS.findMathTransform(sourceCrs, targetCrs);
    }

    public static List<Subject> convertFeaturesToSubjects(FeatureReader<SimpleFeatureType, SimpleFeature> featureReader, SubjectType subjectType, GeotoolsDataStoreImporter importer) throws FactoryException, IOException, TransformException {
        MathTransform crsTransform = makeCrsTransform(importer.getEncoding());

        List<Subject> subjects = new ArrayList<>();
        while (featureReader.hasNext()) {
            SimpleFeature feature = featureReader.next();
            String label = importer.getFeatureSubjectLabel(feature, subjectType);
            String name = importer.getFeatureSubjectName(feature, subjectType);
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            try {
                Geometry transformedGeom = JTS.transform(geom, crsTransform);
                transformedGeom.setSRID(Subject.SRID);
                subjects.add(new Subject(subjectType, label, name, transformedGeom));
            } catch (ProjectionException e) {
                log.warn("Rejecting {}. You will see this if you have assertions enabled (e.g. " +
                        "you run with `-ea`) as GeoTools runs asserts. See source of GeotoolsDataStoreUtils for details on this. " +
                        "To fix this, replace `-ea` with `-ea -da:org.geotools...` in your test VM options (probably in" +
                        "your IDE) to disable assertions in GeoTools.", label);
            }
        }
        return subjects;
    }
}