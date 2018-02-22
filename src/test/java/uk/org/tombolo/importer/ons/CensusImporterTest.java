package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CensusImporterTest extends AbstractTest {
    private static final String MTW_ID = "qs701ew";
    private static final String POP_ID = "qs102ew";
    private static final String POD_ID = "qs303ew";
    private static CensusImporter importer;
    private static List<SubjectRecipe> subjectRecipes = Collections.singletonList(new SubjectRecipe(AbstractONSImporter.PROVIDER.getLabel(),
                                        "lsoa", null, null));

    Subject cityOfLondon01;
    Subject cityOfLondon02;
    Subject islington01;
    Subject islington02;


    @Before
    public void setup() throws Exception {
        importer = new CensusImporter();
        importer.setSubjectRecipes(subjectRecipes);
        mockDownloadUtils(importer);

        cityOfLondon01 = TestFactory.makeNamedSubject("E01000001");
        cityOfLondon02 = TestFactory.makeNamedSubject("E01000002");
        islington01 = TestFactory.makeNamedSubject("E01002766");
        islington02 = TestFactory.makeNamedSubject("E01002767");
    }

    @Test
    public void getTimedValueAttributesMTW() throws Exception {
        importer.importDatasource(MTW_ID, null, null, null, false);
        List<Attribute> attributes = importer.getTimedValueAttributes(MTW_ID);
        assertEquals(13, attributes.size());
    }

    @Test
    public void getTimedValueAttributesPOP() throws Exception {
        importer.importDatasource(POP_ID, null, null, null, false);
        List<Attribute> attributes = importer.getTimedValueAttributes(POP_ID);
        assertEquals(3, attributes.size());
    }

    @Test
    public void getTimedValueAttributesPOD() throws Exception {
        importer.importDatasource(POD_ID, null, null, null, false);
        List<Attribute> attributes = importer.getTimedValueAttributes(POD_ID);
        assertEquals(4, attributes.size());
    }

    @Test
    public void getDataUrl() throws Exception {
        importer.importDatasource(MTW_ID, null, null, null, false);
        assertEquals("https://www.nomisweb.co.uk/api/v01/dataset/nm_568_1.bulk.csv?time=latest&measures=20100&rural_urban=total&geography=TYPE298", importer.getDataUrl(MTW_ID, "lsoa"));
    }

    @Test
    public void importDatasourceMTW() throws Exception {
        importer.importDatasource(MTW_ID, null, null, null, false);
        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
        assertEquals(78, importer.getTimedValueCount());

        Attribute attribute01 = AttributeUtils.getByProviderAndLabel(importer.getProvider(),
                "Method of Travel to Work: All categories: Method of travel to work");

        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon01, attribute01);
        assertEquals(1221d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon02, attribute01);
        assertEquals(1196d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington01, attribute01);
        assertEquals(1366d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington02, attribute01);
        assertEquals(1628d, timedValue.getValue(), 0.0d);

        Attribute attribute02 = AttributeUtils.getByProviderAndLabel(importer.getProvider(),
                "Method of Travel to Work: On foot");

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon01, attribute02);
        assertEquals(445d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon02, attribute02);
        assertEquals(428d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington01, attribute02);
        assertEquals(98d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington02, attribute02);
        assertEquals(122d, timedValue.getValue(), 0.0d);
    }

    @Test
    public void importDatasourcePOP() throws Exception {
        importer.importDatasource(POP_ID, null, null, null, false);
        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
        assertEquals(18, importer.getTimedValueCount());

        Attribute attribute01 = AttributeUtils.getByProviderAndLabel(importer.getProvider(),
                "Area/Population Density: Density (number of persons per hectare)");

        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon01, attribute01);
        assertEquals(112.9d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon02, attribute01);
        assertEquals(62.9d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington01, attribute01);
        assertEquals(131.2d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington02, attribute01);
        assertEquals(171.9d, timedValue.getValue(), 0.0d);
    }

    @Test
    public void importDatasourcePOD() throws Exception {
        importer.importDatasource(POD_ID, null, null, null, false);
        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
        assertEquals(24, importer.getTimedValueCount());

        Attribute attribute01 = AttributeUtils.getByProviderAndLabel(importer.getProvider(),
                "Disability: Day-to-day activities limited a little");

        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon01, attribute01);
        assertEquals(115d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon02, attribute01);
        assertEquals(101d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington01, attribute01);
        assertEquals(171d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington02, attribute01);
        assertEquals(171, timedValue.getValue(), 0.0d);
    }
}