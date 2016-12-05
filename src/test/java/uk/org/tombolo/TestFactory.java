package uk.org.tombolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * TestFactory.java
 * A collection of useful object factories for setting up the database in tests
 */
public final class TestFactory {
    public static final Provider DEFAULT_PROVIDER = new Provider("default_provider_label", "default_provider_name");
    public static final String TIMESTAMP = "2011-01-01T00:00:00";
    public static final Geometry FAKE_POINT_GEOMETRY = makePointGeometry(0d, 0d);

    /**
     * makeFakeGeomtry
     * Returns a point at the offset provided
     * @param xOffset
     * @param yOffset
     * @return A point geometry at xOffset, yOffset
     */
    public static Geometry makePointGeometry(Double xOffset, Double yOffset) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry point =  geometryFactory.createPoint(new Coordinate(xOffset, yOffset));
        point.setSRID(Subject.SRID);
        return point;
    }

    /**
     * Returns a square gometry
     * @param lowerLeftXOffset x-coordinate of lower left corner
     * @param lowerLeftYOffset y-coordinate of lower left corner
     * @param edgeSize the edge size of the square
     * @return A square geometry with left corner at lowerLeftXOffset, lowerLeftYOffset
     */
    public static Geometry makeSquareGeometry(Double lowerLeftXOffset, Double lowerLeftYOffset, Double edgeSize){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] corners = {
                new Coordinate(lowerLeftXOffset, lowerLeftYOffset),
                new Coordinate(lowerLeftXOffset, lowerLeftYOffset+edgeSize),
                new Coordinate(lowerLeftXOffset+edgeSize, lowerLeftYOffset+edgeSize),
                new Coordinate(lowerLeftXOffset+edgeSize, lowerLeftYOffset),
                new Coordinate(lowerLeftXOffset, lowerLeftYOffset)
        };
        Geometry square = geometryFactory.createPolygon(corners);
        square.setSRID(Subject.SRID);
        return square;
    }

    private TestFactory() {}

    /**
     * makeAttribute
     * Builds and persists an Attribute with a given Provider and attributes based on the given label.
     * It will persist the Provider to the database also.
     * Consider using {@link #DEFAULT_PROVIDER} as your provider if you don't have one handy
     * @param provider
     * @param label The label, also used to construct the name & description of the attribute
     * @return The persisted attribute
     */
    public static Attribute makeAttribute(Provider provider, String label) {
        ProviderUtils.save(provider);
        Attribute attribute = new Attribute(provider, label, label + "_name", label + "_description", null);
        AttributeUtils.save(attribute);
        return attribute;
    }

    /**
     * makeTimedValue
     * Builds and persists a TimedValue with the given attributes
     * @param subjectLabel
     * @param attribute
     * @param timestamp
     * @param value
     * @return The persisted TimedValue
     */
    public static TimedValue makeTimedValue(String subjectLabel, Attribute attribute, String timestamp, Double value) {
        Subject subject = SubjectUtils.getSubjectByLabel(subjectLabel);
        TimedValue timedValue = new TimedValue(subject, attribute, LocalDateTime.parse(timestamp), value);
        TimedValueUtils.save(timedValue);
        return timedValue;
    }

    /**
     * makeFixedValue
     * Builds and persists a FixedValue with the given attributes
     * @param subjectLabel
     * @param attribute
     * @param value
     * @return The persisted FixedValue
     */
    public static FixedValue makeFixedValue(String subjectLabel, Attribute attribute, String value) {
        Subject subject = SubjectUtils.getSubjectByLabel(subjectLabel);
        FixedValue fixedValue = new FixedValue(subject, attribute, value);
        FixedValueUtils.save(fixedValue);
        return fixedValue;
    }

    /**
     * makeNamedSubject
     * Builds and persists one of the named Subjects, so you don't have to keep making up attributes.
     * These are realistic values, aside from the geometry which is always a point at 0,0.
     * See the source for a list of available subjects.
     * @param label The label of the namedSubject
     * @return The persisted subject
     */
    public static Subject makeNamedSubject(String label) {
        switch (label) {
            case "E01000001":
                makeNamedSubjectType("lsoa");
                return makeSubject("lsoa", label, "City of London 001A", FAKE_POINT_GEOMETRY);
            case "E01000002":
                makeNamedSubjectType("lsoa");
                return makeSubject("lsoa", label, "City of London 001B", FAKE_POINT_GEOMETRY);
            case "E09000001":
                makeNamedSubjectType("localAuthority");
                return makeSubject("localAuthority", label, "City of London", FAKE_POINT_GEOMETRY);
            case "E09000019":
                makeSubjectType("localAuthority", "Local Authority");
                return makeSubject("localAuthority", label, "Islington", FAKE_POINT_GEOMETRY);
            case "E08000035":
                makeNamedSubjectType("localAuthority");
                return makeSubject("localAuthority", label, "Leeds", FAKE_POINT_GEOMETRY);
            case "E01002766":
                makeNamedSubjectType("lsoa");
                return makeSubject("lsoa", label, "Islington 015E", FAKE_POINT_GEOMETRY);
            case "E01002767":
                makeNamedSubjectType("lsoa");
                return makeSubject("lsoa", label, "Islington 011D", FAKE_POINT_GEOMETRY);
            case "E02000001":
                makeNamedSubjectType("msoa");
                return makeSubject("msoa", label, "City of London 001", FAKE_POINT_GEOMETRY);
            case "E02000564":
                makeNamedSubjectType("msoa");
                return makeSubject("msoa", label, "Islington 011", FAKE_POINT_GEOMETRY);
            default:
                throw new IllegalArgumentException(String.format("%s is not a valid named subject fixture, see TestFactory#makeNamedSubject for a list of valid subject labels.", label));
        }
    }

    private static SubjectType makeNamedSubjectType(String label) {
        switch (label) {
            case "localAuthority":
                return makeSubjectType("localAuthority", "Local Authority");
            case "lsoa":
                return makeSubjectType("lsoa", "Lower Super Output Area");
            case "msoa":
                return makeSubjectType("msoa", "Middle Super Output Area");
        }
        return null;
    }

    /**
     * makeSubjectType
     * Either retrieves, or builds and persists a SubjectType with the attributes specified
     * @param label
     * @param name
     * @return The persisted subject
     */
    public static SubjectType makeSubjectType(String label, String name) {
        return SubjectTypeUtils.getOrCreate(label, name);
    }
    
    public static Subject makeSubject(String subjectTypeLabel, String label, String name, Geometry geometry) {
        Subject subject = new Subject(SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel), label, name, geometry);
        SubjectUtils.save(Collections.singletonList(subject));
        return subject;
    }
}
