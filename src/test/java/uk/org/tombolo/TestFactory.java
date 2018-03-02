package uk.org.tombolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.ons.OaImporter;

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
     * makeFakeGeometry
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
     * makeLineStringGeometry
     * Returns a lineString at the offset provided
     * @param xOffset
     * @param yOffset
     * @param length length of the line
     * @return A LineString geometry from xOffset, yOffset to xOffset + length, yOffset + length
     */
    public static Geometry makeLineStringGeometry(Double xOffset, Double yOffset, Double length) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = {new Coordinate(xOffset, yOffset),
                new Coordinate(xOffset + length, yOffset)};
        Geometry lineString =  geometryFactory.createLineString(coordinates);
        lineString.setSRID(Subject.SRID);
        return lineString;
    }

    /**
     * Make LineString given the coordinates
     * @param coordinates
     * @return
     */
    public static Geometry makeLineStringGeometry(Coordinate[] coordinates) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry lineString =  geometryFactory.createLineString(coordinates);
        lineString.setSRID(Subject.SRID);
        return lineString;
    }

    /**
     * Returns a square geometry
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
        Attribute attribute = new Attribute(provider, label,label + "_description");
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
    public static TimedValue makeTimedValue(SubjectType subjectType, String subjectLabel, Attribute attribute, String timestamp, Double value) {
        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, subjectLabel);
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
    public static FixedValue makeFixedValue(SubjectType subjectType, String subjectLabel, Attribute attribute, String value) {
        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, subjectLabel);
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
        SubjectType subjectType;
        switch (label) {
            case "E01000001":
                subjectType = makeNamedSubjectType("lsoa");
                return makeSubject(subjectType, label, "City of London 001A", FAKE_POINT_GEOMETRY);
            case "E01000002":
                subjectType = makeNamedSubjectType("lsoa");
                return makeSubject(subjectType, label, "City of London 001B", FAKE_POINT_GEOMETRY);
            case "E09000001":
                subjectType = makeNamedSubjectType("localAuthority");
                return makeSubject(subjectType, label, "City of London", FAKE_POINT_GEOMETRY);
            case "E09000002":
                subjectType = makeNamedSubjectType("localAuthority");
                return makeSubject(subjectType, label, "Barking and Dagenham", FAKE_POINT_GEOMETRY);
            case "E09000019":
                subjectType = makeNamedSubjectType("localAuthority");
                return makeSubject(subjectType, label, "Islington", FAKE_POINT_GEOMETRY);
            case "E08000035":
                subjectType = makeNamedSubjectType("localAuthority");
                return makeSubject(subjectType, label, "Leeds", FAKE_POINT_GEOMETRY);
            case "E01002766":
                subjectType = makeNamedSubjectType("lsoa");
                return makeSubject(subjectType, label, "Islington 015E", FAKE_POINT_GEOMETRY);
            case "E01002767":
                subjectType = makeNamedSubjectType("lsoa");
                return makeSubject(subjectType, label, "Islington 011D", FAKE_POINT_GEOMETRY);
            case "E02000001":
                subjectType = makeNamedSubjectType("msoa");
                return makeSubject(subjectType, label, "City of London 001", FAKE_POINT_GEOMETRY);
            case "E02000564":
                subjectType = makeNamedSubjectType("msoa");
                return makeSubject(subjectType, label, "Islington 011", FAKE_POINT_GEOMETRY);
            case "E05000371":
                subjectType = makeNamedSubjectType("ward");
                return makeSubject(subjectType, label, "Finsbury Park", FAKE_POINT_GEOMETRY);
            case "E12000001":
                subjectType = makeNamedSubjectType("englandBoundaries");
                return makeSubject(subjectType, label, "North East", FAKE_POINT_GEOMETRY);
            default:
                throw new IllegalArgumentException(String.format("%s is not a valid named subject fixture, see TestFactory#makeNamedSubject for a list of valid subject labels.", label));
        }
    }

    public static SubjectType makeNamedSubjectType(String label) {
        SubjectType subjectType;
        switch (label) {
            case "englandBoundaries":
                subjectType = OaImporter.getSubjectType(OaImporter.OaType.englandBoundaries);
                break;
            case "localAuthority":
                subjectType = OaImporter.getSubjectType(OaImporter.OaType.localAuthority);
                break;
            case "lsoa":
                subjectType = OaImporter.getSubjectType(OaImporter.OaType.lsoa);
                break;
            case "msoa":
                subjectType = OaImporter.getSubjectType(OaImporter.OaType.msoa);
                break;
            case "ward":
                subjectType = OaImporter.getSubjectType(OaImporter.OaType.ward);
                break;
            default:
                return null;
        }
        return makeSubjectType(subjectType.getProvider(), subjectType.getLabel(), subjectType.getName());
    }

    /**
     * makeSubjectType
     * Either retrieves, or builds and persists a SubjectType with the attributes specified
     * @param label
     * @param name
     * @return The persisted subject
     */
    public static SubjectType makeSubjectType(Provider provider, String label, String name) {
        return SubjectTypeUtils.getOrCreate(provider, label, name);
    }

    public static Subject makeSubject(SubjectType subjectType, String label, String name, Geometry geometry) {
        Subject subject = new Subject(subjectType, label, name, geometry);
        SubjectUtils.save(Collections.singletonList(subject));
        return subject;
    }
}
