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
        return geometryFactory.createPoint(new Coordinate(xOffset, yOffset));
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
                makeSubjectType("lsoa", "Lower Super Output Area");
                return makeSubject("lsoa", label, "City of London 001A", FAKE_POINT_GEOMETRY);
            case "E09000001":
                makeSubjectType("localAuthority", "Local Authority");
                return makeSubject("localAuthority", label, "City of London", FAKE_POINT_GEOMETRY);
            case "E08000035":
                makeSubjectType("localAuthority", "Local Authority");
                return makeSubject("localAuthority", label, "Leeds", FAKE_POINT_GEOMETRY);
            case "E01002766":
                makeSubjectType("lsoa", "Lower Super Output Area");
                return makeSubject("lsoa", label, "Islington 015E", FAKE_POINT_GEOMETRY);
            default:
                throw new IllegalArgumentException(String.format("%s is not a valid named subject fixture, see TestFactory#makeNamedSubject for a list of valid subject labels.", label));
        }
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
    
    private static Subject makeSubject(String subjectTypeLabel, String label, String name, Geometry geometry) {
        Subject subject = new Subject(SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel), label, name, geometry);
        SubjectUtils.save(Collections.singletonList(subject));
        return subject;
    }
}
