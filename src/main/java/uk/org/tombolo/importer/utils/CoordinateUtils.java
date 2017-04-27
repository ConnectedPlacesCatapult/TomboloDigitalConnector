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
    private static final String OSGBCRS = "EPSG:27700";

    public static Coordinate eastNorthToLatLong(long easting, long northing, String crs) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {
        CoordinateReferenceSystem wgs84crs = CRS.decode(WGS84CRS);
        CoordinateReferenceSystem osgbCrs = CRS.decode(crs);

        CoordinateOperation op = new DefaultCoordinateOperationFactory().createOperation(osgbCrs, wgs84crs);

        DirectPosition eastNorth = new GeneralDirectPosition(easting, northing);
        DirectPosition latLng = op.getMathTransform().transform(eastNorth, null);
        Double latitude = latLng.getOrdinate(0);
        Double longitude = latLng.getOrdinate(1);

        return new Coordinate(longitude, latitude);
    }

    public static Coordinate osgbToWgs84(long easting, long northing) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {
        return eastNorthToLatLong(easting, northing, OSGBCRS);
    }
}