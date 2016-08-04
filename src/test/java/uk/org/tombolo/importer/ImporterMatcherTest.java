package uk.org.tombolo.importer;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.importer.ImporterMatcher;

import static org.junit.Assert.*;

public class ImporterMatcherTest extends AbstractTest {

    @Test
    public void testDoesMatchSingle() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("com.MatchMe:match-me");
        assertTrue(matcher.doesMatch("com.MatchMe", "match-me"));
        assertFalse(matcher.doesMatch("com.DontMatchMe", "dont-match-me"));
    }

    @Test
    public void testDoesMatchTwo() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("com.MatchMe:match-me, com.MatchMeToo:match-me-too");
        assertTrue(matcher.doesMatch("com.MatchMe", "match-me"));
        assertTrue(matcher.doesMatch("com.MatchMeToo", "match-me-too"));
        assertFalse(matcher.doesMatch("com.DontMatchMe", "dont-match-me"));
    }

    @Test
    public void testDoesMatchWildcardClass() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("com.MatchAllOfMe: , com.MatchMeToo:match-me-only");
        assertTrue(matcher.doesMatch("com.MatchAllOfMe", "match-me"));
        assertTrue(matcher.doesMatch("com.MatchMeToo", "match-me-only"));
        assertFalse(matcher.doesMatch("com.MatchMeToo", "dont-match-me"));
        assertFalse(matcher.doesMatch("com.DontMatchMe", "dont-match-me-either"));
    }

    @Test
    public void testDoesMatchWildcardDatasource() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("com.MatchMe:match-me, :match-all-of-me");
        assertTrue(matcher.doesMatch("com.MatchMe", "match-me"));
        assertTrue(matcher.doesMatch("com.MatchMeToo", "match-all-of-me"));
        assertFalse(matcher.doesMatch("com.MatchMeToo", "dont-match-me"));
        assertFalse(matcher.doesMatch("com.DontMatchMe", "dont-match-me-either"));
    }

    @Test
    public void testDoesMatchWildcardAnything() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher(":");
        assertTrue(matcher.doesMatch("com.MatchMe", "match-me"));
        assertTrue(matcher.doesMatch("com.MatchMeToo", "match-all-of-me"));
    }

    @Test
    public void testDoesMatchNothingWithNull() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher(null);
        assertFalse(matcher.doesMatch("com.MatchMe", "match-me"));
    }

    @Test
    public void testDoesMatchNothingWithEmptyString() throws Exception {
        ImporterMatcher matcher = new ImporterMatcher("");
        assertFalse(matcher.doesMatch("com.MatchMe", "match-me"));
    }
}