package uk.org.tombolo.field.transformation;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;

import static junit.framework.Assert.assertEquals;

/**
 * Test for the subject area field.
 */
public class AreaFieldTest extends AbstractTest {

    @Test
    public void testAreaField() throws Exception{
        SubjectType subjectType = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER,
                "areaSubjectType",
                "areaSubjectType"
        );

        AreaField areaField = new AreaField("area");

        Subject subjectSquare5 = TestFactory.makeSubject(subjectType,
                "square5",
                "square5",
                TestFactory.makeSquareGeometry(0d, 0d, 5d)
        );
        assertEquals(areaField.valueForSubject(subjectSquare5), "25.0");

        Subject subjectSquare7 = TestFactory.makeSubject(subjectType,
                "square7",
                "square7",
                TestFactory.makeSquareGeometry(5d, 5d, 7d)
        );
        assertEquals(areaField.valueForSubject(subjectSquare7), "49.0");

        Subject subjectPoint = TestFactory.makeSubject(subjectType,
                "point",
                "point",
                TestFactory.makePointGeometry(10d, 10d)
        );
        assertEquals(areaField.valueForSubject(subjectPoint), "0.0");

        Subject subjectLine = TestFactory.makeSubject(subjectType,
                "line",
                "line",
                TestFactory.makeLineStringGeometry(2d, 3d, 15d)
        );
        assertEquals(areaField.valueForSubject(subjectLine), "0.0");
    }

}
