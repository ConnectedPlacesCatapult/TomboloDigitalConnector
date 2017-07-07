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
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

/**
 * Field that represents the area of the subject's geography.
 * It will give the area only for polygons and multi-polygons,
 * the other shapes(e.g. Points) will return 0.0.
 */
public class AreaField implements SingleValueField {

    private final String label;
    private final int targetCRSCode;

    public AreaField(String label, int targetCRSCode) {
        this.label = label;
        this.targetCRSCode = targetCRSCode;
    }

    private String getTransformedArea(Subject subject) {
        Geometry geometry = subject.getShape();

        if (Subject.SRID != targetCRSCode) {
            try {
                CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + Subject.SRID);
                CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + targetCRSCode);
                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                geometry = JTS.transform(geometry, transform);
            } catch (FactoryException e) {
                e.printStackTrace();
            } catch (TransformException e) {
                e.printStackTrace();
            }
        }

        return String.format("%.02f", geometry.getArea());
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return getTransformedArea(subject);
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, Double.valueOf(getTransformedArea(subject)));
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
