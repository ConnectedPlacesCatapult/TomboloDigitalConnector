package uk.org.tombolo.importer.twitter;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.ZipUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Tests the import for twitter data, for both cases when the streaming api or search api.
 */

public class TwitterImporterTest extends AbstractTest {
    private static TwitterImporter importer;

    private static final String LOCAL_DATA_STREAMING_API =
            "src/test/resources/datacache/TomboloData/com.twitter/StreamingAPIResult.json.gz";
    private static final String LOCAL_DATA_SEARCH_API =
            "src/test/resources/datacache/TomboloData/com.twitter/searchAPIResult.json";

    @Before
    public void before(){
        importer = new TwitterImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void isGZipped() {
        assertEquals(true, ZipUtils.isGZipped(new File(LOCAL_DATA_STREAMING_API)));
        assertEquals(false, ZipUtils.isGZipped(new File(LOCAL_DATA_SEARCH_API)));

    }
    @Test
    public void getDatasource() throws Exception {
        List<String> datasources = importer.getDatasourceIds();

        assertEquals(1, datasources.size());
        assertEquals("twitter", datasources.get(0));
    }

    @Test
    public void getAttribute() throws Exception {
        importer.importDatasource("twitter", null, null, Arrays.asList(LOCAL_DATA_SEARCH_API));
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "user");
        assertEquals("com.twitter", attribute.getProvider().getLabel());
        assertEquals("user", attribute.getLabel());
        assertEquals("user screen name", attribute.getDescription());

        importer.importDatasource("twitter", null, null, Arrays.asList(LOCAL_DATA_STREAMING_API));
        Attribute attribute1 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "user");
        assertEquals("com.twitter", attribute1.getProvider().getLabel());
        assertEquals("user", attribute1.getLabel());
        assertEquals("user screen name", attribute1.getDescription());
    }

    @Test
    public void importDatasorce() throws Exception {
        importer.importDatasource("twitter", null, null, Arrays.asList(LOCAL_DATA_SEARCH_API));

        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("com.twitter","Tweet"),"250075927172759552");
        assertEquals("sean_cummings_250075927172759552", subject.getName());
        assertNull(subject.getShape().getCoordinate());

        List<String> attributes = Arrays.asList("user", "description", "location", "account_creation","utc_offset",
                "followers", "following", "text", "id", "timestamp", "retweet", "source");
        List<String> values = Arrays.asList("sean_cummings", "Born 330 Live 310", "LA, CA", "Mon Apr 26 06:01:55 UTC 2010",
                "-28800","70", "110", "Aggressive Ponytail #freebandnames", "250075927172759552",
                "Mon Sep 24 03:35:21 UTC 2012", "0", "Twitter for Mac");

        for (int i = 0; i < attributes.size(); i++) {
            testFixedValue(subject, attributes.get(i), values.get(i));
        }

        importer.importDatasource("twitter", null, null, Arrays.asList(LOCAL_DATA_STREAMING_API));

        subject = SubjectUtils.getSubjectByTypeAndLabel(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("com.twitter","Tweet"),"808282128278372352");
        assertEquals("naturalkitchen_808282128278372352", subject.getName());
        assertEquals("-0.151667", subject.getShape().getCoordinate().x + "");
        assertEquals("51.5197", subject.getShape().getCoordinate().y + "");

        attributes = Arrays.asList("user", "description", "location", "account_creation","utc_offset",
                "followers", "following", "text", "id", "coordinates","timestamp", "retweet", "source");
        values = Arrays.asList("naturalkitchen",
                "We love good food. Whether it is our fresh fruit & vegetables, fish or meat from our handful of suppliers everything is selected on taste and quality first." ,
                "London, UK", "Tue May 05 22:09:56 UTC 2009", "0", "2153", "210",
                "It's beginning to look a lot like Christmas in Marylebone!!! @Marylebone High Street https://t.co/FmjBA6w30l",
                "808282128278372352", "GeoLocation{latitude=51.5197, longitude=-0.151667}", "Mon Dec 12 12:07:31 UTC 2016", "0", "<a href=\"http://instagram.com\" rel=\"nofollow\">Instagram</a>");

        for (int i = 0; i < attributes.size(); i++) {
            testFixedValue(subject, attributes.get(i), values.get(i));
        }
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
