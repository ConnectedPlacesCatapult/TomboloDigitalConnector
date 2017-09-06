package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;

import static org.junit.Assert.assertEquals;

public class AttributeUtilsTest extends AbstractTest {

    @Test
    public void substringToDBLengthTest() throws Exception {
        String longString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt " +
                "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
                "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit " +
                "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
                "culpa qui officia deserunt mollit anim id est laborum.";

        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt " +
                "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
                "nisi ut aliquip ex ea commo db89bb5c-eab8-3f9c-8fcc-2ab36c189c2c";

        assertEquals(expected, AttributeUtils.substringToDBLength(longString));
    }

}
