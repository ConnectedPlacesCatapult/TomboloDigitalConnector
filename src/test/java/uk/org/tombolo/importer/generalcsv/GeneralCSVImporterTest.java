package uk.org.tombolo.importer.generalcsv;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractRunner;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.utils.ConfigUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * File ZmlsZTovVXNlcnMvbHFlbmRyby9Ub21ib2xvRGlnaXRhbENvbm5lY3Rvci9zcmMvdGVzdC9yZXNvdXJjZXMvZGF0YWNhY2hlL1RvbWJvbG9EYXRhL2dlbmVyYWwuY3N2LnByb3ZpZGVyL3Rlc3RHZW5lcmFsQ1NWRmlsZUdlby5jc3Y=.csv
 */
public class GeneralCSVImporterTest extends AbstractTest {
    private GeneralCSVImporter importer;

    private static final String LOCATION_FILE = "src/test/resources/datacache/TomboloData/general.csv.provider/testGeneralCSVFileGeo.csv";
    private static final SubjectType DEFAULT_SUBJECT_TYPE = new SubjectType(
            new Provider("subject.type.provider", ""),
            "lsoa",
            ""
    );
    private static final Config.Builder EXISTING_SUBJECT_CONFIG = new Config.Builder(
            0,
            "yes",
            LOCATION_FILE,
            "general.csv.provider",
            DEFAULT_SUBJECT_TYPE
    );

    private static final Config.Builder NEW_SUBJECT_WITHOUT_GEO = new Config.Builder(
            0,
            "no",
            LOCATION_FILE,
            "general.csv.provider",
            DEFAULT_SUBJECT_TYPE
    );

    private static Config.Builder NEW_SUBJECT_WITH_GEO = new Config.Builder(
            0,
            "no",
            LOCATION_FILE,
            "general.csv.provider",
            DEFAULT_SUBJECT_TYPE
    ).geography("EPSG:4326", 2, 1);

    private static Config.Builder NEW_SUBJECT_GEO_EAST_NORTH = new Config.Builder(
            0,
            "no",
            LOCATION_FILE,
            "general.csv.provider",
            DEFAULT_SUBJECT_TYPE)
            .geography("EPSG:27700", 2, 1);

    @Before
    public void before(){
        importer = new GeneralCSVImporter(NEW_SUBJECT_WITHOUT_GEO.build());
        mockDownloadUtils(importer);
    }

    @Test
    public void testConfigUtils() throws Exception {
        Config testConfig = NEW_SUBJECT_WITH_GEO.build();
        Config fileConfig = ConfigUtils.loadConfig(AbstractRunner.loadProperties("Configuration file", "src/test/resources/datacache/TomboloData/general.csv.provider/config.properties"));

        assertEquals(testConfig.getExistingSubject(), fileConfig.getExistingSubject());
        assertEquals(testConfig.getFileLocation(), fileConfig.getFileLocation());
        assertEquals(testConfig.getGeographyProjection(), testConfig.getGeographyProjection());
        assertEquals(testConfig.getGeographyXIndex(), fileConfig.getGeographyXIndex());
        assertEquals(testConfig.getGeographyYIndex(), fileConfig.getGeographyYIndex());
        assertEquals(testConfig.getProvider(), fileConfig.getProvider());
        assertEquals(testConfig.getSubjectIDIndex(), testConfig.getSubjectIDIndex());
        assertEquals(testConfig.getSubjectType(), testConfig.getSubjectType() );
    }

    @Test
    public void testGetProvider(){
        Provider provider = importer.getProvider();
        assertEquals("general.csv.provider", provider.getLabel());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(1,datasources.size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("datasourceGeneral.csv.provider");
        assertEquals(LOCATION_FILE, datasource.getRemoteDatafile());
    }

    @Test
    public void testImportDatasourceNoGeo() throws Exception {
        // Testing import with new subject but no geography
        importer.importDatasource("datasourceGeneral.csv.provider");

        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(
                SubjectTypeUtils.getSubjectTypeByProviderAndLabel("subject.type.provider","lsoa"),
                "E01000001");
        assertEquals("E01000001", subject.getLabel());

        testImport(subject);
    }

    @Test
    public void testImportDatasourceExSubject() throws Exception {
        Subject subject = TestFactory.makeNamedSubject(new Provider("subject.type.provider", ""), "E01000001");
        TestFactory.makeNamedSubject(new Provider("subject.type.provider", ""), "E01000002");

        // Testing import with existing subject
        importer.setConfig(EXISTING_SUBJECT_CONFIG.build());
        importer.importDatasource("datasourceGeneral.csv.provider");

        testImport(subject);
    }

    @Test
    public void testImportDatasourceWithGeo() throws Exception {
        // Testing import with new subject with geography
        importer.setConfig(NEW_SUBJECT_WITH_GEO.build());
        importer.importDatasource("datasourceGeneral.csv.provider");

        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(
                SubjectTypeUtils.getSubjectTypeByProviderAndLabel("subject.type.provider","lsoa"),
                "E01000001");

        assertEquals(-2.8906469345092773, subject.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(53.482330322265625, subject.getShape().getCentroid().getY(), 0.1E-6);
    }

    @Test
    public void testImportDatasorceEastNorth()  throws Exception {
        importer.setConfig(NEW_SUBJECT_GEO_EAST_NORTH.build());
        importer.importDatasource("datasourceGeneral.csv.provider");

        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(
                SubjectTypeUtils.getSubjectTypeByProviderAndLabel("subject.type.provider","lsoa"),
                "E01000002");

        assertEquals(-0.117539388132, subject.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.5566037738, subject.getShape().getCentroid().getY(), 0.1E-6);
    }

    private void testImport(Subject subject) {
        String header = "lat(north),long(east),attribute1,attribute2,attribute3,attribute4,attribute5,attribute6";
        String value = "53.482330322265625,-2.8906469345092773,value01,value10,value11,value100,value101,value110";

        String[] headers = header.split(",");
        String[] values = value.split(",");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, AttributeUtils.nameToLabel(headers[i]), values[i]);
        }
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
