package uk.org.tombolo.field.transformation;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.simple.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

/**
 * Field that represents the area of the subject's geography.
 * It will give the area only for polygons and multi-polygons,
 * the other shapes(e.g. Points) will return 0.0.
 */
public class AreaField extends AbstractField implements SingleValueField {

    private final int targetCRSCode;

    public AreaField(String label, int targetCRSCode) {
        super(label);
        this.targetCRSCode = targetCRSCode;
    }

    private String getTransformedArea(Subject subject) throws IncomputableFieldException{
        String cachedValue = getCachedValue(subject);
        if (cachedValue == null) {
            Geometry geometry = subject.getShape();

            if (Subject.SRID != targetCRSCode) {
                try {
                    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + Subject.SRID);
                    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + targetCRSCode);
                    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                    geometry = JTS.transform(geometry, transform);
                } catch (FactoryException e) {
                    throw new IncomputableFieldException(String.format("No math transform can be created for source Coordinate " +
                            "Reference System (%s) and target (%s)", Subject.SRID, targetCRSCode));
                } catch (TransformException e) {
                    throw new IncomputableFieldException(String.format("Cannot transform coordinate from source Coordinate " +
                            "Reference System (%s) to target (%s)", Subject.SRID, targetCRSCode));
                }
            }
            cachedValue = String.format("%.02f", geometry.getArea());
            setCachedValue(subject, cachedValue);
        }

        return cachedValue;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getTransformedArea(subject);
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, Double.valueOf(getTransformedArea(subject)));
        return obj;
    }

}
