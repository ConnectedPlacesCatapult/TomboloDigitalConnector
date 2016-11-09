package uk.org.tombolo.importer.utils;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

import java.io.IOException;

public abstract class GeotoolsDataStoreUtils extends AbstractImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(GeotoolsDataStoreUtils.class);

    public static FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(DataStore store, String typeName) throws IOException {
        DefaultQuery query = new DefaultQuery(typeName);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public static MathTransform makeCrsTransform(String inputFormat) throws FactoryException {
        CoordinateReferenceSystem sourceCrs = CRS.decode(inputFormat);
        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:"+Subject.SRID, true);

        return CRS.findMathTransform(sourceCrs, targetCrs);
    }
}