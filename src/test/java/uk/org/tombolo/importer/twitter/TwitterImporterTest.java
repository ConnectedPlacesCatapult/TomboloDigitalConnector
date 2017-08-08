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
import java.util.List;

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
        assertEquals("Twitter", datasources.get(0));
    }

    @Test
    public void getAttribute() throws Exception {
        importer.importDatasource("Twitter", null, null, LOCAL_DATA_SEARCH_API);
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "user");
        assertEquals("com.twitter", attribute.getProvider().getLabel());
        assertEquals("user", attribute.getLabel());
        assertEquals("user", attribute.getName());

        importer.importDatasource("Twitter", null, null, LOCAL_DATA_STREAMING_API);
        Attribute attribute1 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "user");
        assertEquals("com.twitter", attribute1.getProvider().getLabel());
        assertEquals("user", attribute1.getLabel());
        assertEquals("user", attribute1.getName());
    }

    @Test
    public void importDatasorce() throws Exception {
        System.out.print(System.getProperty("user.dir"));

        importer.importDatasource("Twitter", null, null, LOCAL_DATA_SEARCH_API);

        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("com.twitter","Tweet"),"250075927172759552");
        assertEquals("Sean_Cummings250075927172759552", subject.getName());

        String header = "user;description;location;followers;following;text;id;coordinates;retweet;source\n";
        String value = "Sean Cummings;Born 330 Live 310;LA, CA;70;110;Aggressive Ponytail #freebandnames;250075927172759552;;0;Twitter for Mac\n";
        String[] headers = header.split("[;\n]");
        String[] values = value.split("[;\n]");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, headers[i], values[i]);
        }

        importer.importDatasource("Twitter", null, null, LOCAL_DATA_STREAMING_API);

        subject = SubjectUtils.getSubjectByTypeAndLabel(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("com.twitter","Tweet"),"808282128278372352");
        assertEquals("Natural_Kitchen808282128278372352", subject.getName());

        value = "Natural Kitchen;We love good food. Whether it is our fresh fruit & vegetables, fish or meat from our handful of suppliers everything is selected on taste and quality first." +
                ";London, UK;2153;210;It's beginning to look a lot like Christmas in Marylebone!!! \uD83C\uDF85\uD83C\uDFFD\uD83C\uDF81\uD83C\uDF84\uD83C\uDF85\uD83C\uDFFD\uD83C\uDF81\uD83C\uDF84 @ Marylebone High Street https://t.co/FmjBA6w30l;" +
                "808282128278372352;(-0.151667, 51.5197, NaN);0;<a href=\"http://instagram.com\" rel=\"nofollow\">Instagram</a>\n\n";
        values = value.split("[;\n]");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, headers[i], values[i]);
        }


    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
