package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;

public abstract class AbstractLondonDatastoreTestUtil extends AbstractTest {


    Subject cityOfLondon;
    Subject islington;

    @Before
    public void addSubjectFixtures() {
        cityOfLondon = TestFactory.makeNamedSubject("E09000001");
        islington = TestFactory.makeNamedSubject("E09000019");
    }

}
