package uk.org.tombolo.importer.utils;

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import uk.org.tombolo.importer.DownloadUtils;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    /*
        Returns a map of all UK outcodes (first part of the postcode before the space) to the respective lat-long coordinates.
        The download file is: https://www.freemaptools.com/download/outcode-postcodes/postcode-outcodes.csv
        Local file: aHR0cHM6Ly93d3cuZnJlZW1hcHRvb2xzLmNvbS9kb3dubG9hZC9vdXRjb2RlLXBvc3Rjb2Rlcy9wb3N0Y29kZS1vdXRjb2Rlcy5jc3Y=.csv
     */

    private static final String POSTCODE_TO_COORDINATE_URL = "https://www.freemaptools.com/download/outcode-postcodes/postcode-outcodes.csv";

    public static Map<String, LatLong> postcodeToLatLong(String datasetProvider, DownloadUtils downloadUtils) throws Exception {
        Map<String, LatLong> postcodeToCoordMap = new HashMap<>();
        InputStreamReader postcodeIsr = new InputStreamReader(downloadUtils.fetchInputStream(new URL(POSTCODE_TO_COORDINATE_URL), datasetProvider, ".csv"));

        CSVParser csvFileParser = new CSVParser(postcodeIsr, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iter = csvFileParser.getRecords().iterator();
        CSVRecord header = iter.next();

        while (iter.hasNext()) {
            CSVRecord record = iter.next();
            postcodeToCoordMap.put(record.get(1),
                    new LatLong(record.get(2), record.get(3)));
        }

        return postcodeToCoordMap;
    }
}