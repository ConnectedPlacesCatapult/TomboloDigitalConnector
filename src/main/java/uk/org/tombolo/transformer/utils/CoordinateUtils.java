package uk.org.tombolo.transformer.utils;

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

	public static Coordinate osgbToWgs84(long easting, long northing) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException{
        CoordinateReferenceSystem wgs84crs = CRS.decode("EPSG:4326"); 
        CoordinateReferenceSystem osgbCrs = CRS.decode("EPSG:27700");
		
        CoordinateOperation op = new DefaultCoordinateOperationFactory().createOperation(osgbCrs, wgs84crs);

        DirectPosition eastNorth = new GeneralDirectPosition(easting, northing);
        DirectPosition latLng = op.getMathTransform().transform(eastNorth, null);
        Double latitude = latLng.getOrdinate(0);
        Double longitude = latLng.getOrdinate(1);
        	            
		return new Coordinate(longitude,latitude);
	}
	
}
