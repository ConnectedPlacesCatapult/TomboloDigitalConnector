package uk.org.tombolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.*;

import java.time.LocalDateTime;
import java.util.Collections;

public final class TestFactory {
    public static final Provider DEFAULT_PROVIDER = new Provider("default_provider_label", "default_provider_name");
    public static final String TIMESTAMP = "2011-01-01T00:00:00";
    private static final Geometry FAKE_POINT_GEOMETRY = makeFakeGeometry();

    private static Geometry makeFakeGeometry() {
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createPoint(new Coordinate(0d, 0d));
    }

    private TestFactory() {}

    public static Attribute makeAttribute(Provider provider, String prefix) {
        ProviderUtils.save(provider);
        Attribute attribute = new Attribute(provider, prefix + "_label", prefix + "_name", prefix + "_description", null);
        AttributeUtils.save(attribute);
        return attribute;
    }

    public static TimedValue makeTimedValue(String subjectLabel, Attribute attribute, String timestamp, Double value) {
        Subject subject = SubjectUtils.getSubjectByLabel(subjectLabel);
        TimedValue timedValue = new TimedValue(subject, attribute, LocalDateTime.parse(timestamp), value);
        (new TimedValueUtils()).save(timedValue);
        return timedValue;
    }

    public static Subject makeNamedSubject(String label) {
        switch (label) {
            case "E01000001":
                return makeSubject("lsoa", label, "City of London 001A", FAKE_POINT_GEOMETRY);
            case "E09000001":
                return makeSubject("localAuthority", label, "City of London", FAKE_POINT_GEOMETRY);
            case "E08000035":
                return makeSubject("localAuthority", label, "Leeds", FAKE_POINT_GEOMETRY);
            case "E01002766":
                return makeSubject("lsoa", label, "Islington 015E", FAKE_POINT_GEOMETRY);
            default:
                throw new IllegalArgumentException(String.format("%s is not a valid named subject fixture", label));
        }
    }

    private static Subject makeSubject(String subjectTypeLabel, String label, String name, Geometry geometry) {
        Subject subject = new Subject(SubjectTypeUtils.getSubjectTypeByLabel(subjectTypeLabel), label, name, geometry);
        SubjectUtils.save(Collections.singletonList(subject));
        return subject;
    }
}
