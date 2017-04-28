package uk.org.tombolo.importer.utils;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.TransformException;

public class CoordinateUtils {

    public static final String WGS84CRS = "EPSG:4326";
    public static final String OSGBCRS = "EPSG:27700";

    public static Coordinate eastNorthToLatLong(double x, double y, String sourceCrs, String targetCrs) throws FactoryException, MismatchedDimensionException, TransformException {
        CoordinateReferenceSystem targetCrsDecoded = CRS.decode(targetCrs);
        CoordinateReferenceSystem sourceCrsDecoded = CRS.decode(sourceCrs);

        CoordinateOperation op = new DefaultCoordinateOperationFactory().createOperation(sourceCrsDecoded, targetCrsDecoded);

        DirectPosition source = new GeneralDirectPosition(x, y);
        DirectPosition target = op.getMathTransform().transform(source, null);
        Double targetX = target.getOrdinate(0);
        Double targetY = target.getOrdinate(1);

        return new Coordinate(targetY, targetX);
    }

    public static Coordinate osgbToWgs84(double easting, double northing) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {
        return eastNorthToLatLong(easting, northing, OSGBCRS, WGS84CRS);
    }
}