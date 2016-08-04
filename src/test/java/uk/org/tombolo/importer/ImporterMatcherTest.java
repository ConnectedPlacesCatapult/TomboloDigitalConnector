package uk.org.tombolo.importer;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.importer.ImporterMatcher;

import static org.junit.Assert.*;

public class ImporterMatcherTest extends AbstractTest {

    @Test
    public void testDoesMatchSingle() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("com.MatchMe");
        assertTrue(matcher.doesMatch("com.MatchMe"));
        assertFalse(matcher.doesMatch("com.DontMatchMe"));
    }

    @Test
    public void testDoesMatchTwo() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("com.MatchMe, com.MatchMeToo");
        assertTrue(matcher.doesMatch("com.MatchMe"));
        assertTrue(matcher.doesMatch("com.MatchMeToo"));
        assertFalse(matcher.doesMatch("com.DontMatchMe"));
    }

    @Test
    public void testDoesMatchNothingWithNull() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher(null);
        assertFalse(matcher.doesMatch("com.MatchMe"));
    }

    @Test
    public void testDoesMatchNothingWithEmptyString() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("");
        assertFalse(matcher.doesMatch("com.MatchMe"));
    }
}