package uk.org.tombolo.lac;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.lac.LAQNImporter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * LAQNImporterTest file
 */
public class LAQNImporterTest extends AbstractTest {

    private LAQNImporter laqnImporter;
    private Config defaultConfig = new Config.Builder(0, "",
                            "", "erg.kcl.ac.uk",
                            new SubjectType(new Provider("erg.kcl.ac.uk",
                                    "Environmental Research Group Kings College London"),
                                    "airQualityControl", "Quantity of gases in air by Kings College London"))
            .build();


    @Before
    public void setUp() throws Exception {

        laqnImporter = new LAQNImporter(defaultConfig);
        mockDownloadUtils(laqnImporter);

    }


    private Object allowAccessToPrivateMethods(String methodName,
                                               String... args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Class<?> theClass = Class.forName("uk.org.tombolo.importer.lac.LAQNImporter");
        Constructor<?> constructor = theClass.getConstructor(Config.class);
        Object object = constructor.newInstance(defaultConfig);

        Method method;
        if (args.length > 0) {
            Class[] argTypes = methodName.equals("shape") || methodName.equals("importerURL") ?
                                new Class[] { String.class, String.class } :
                                new Class[] { String.class };
            method = object.getClass().getDeclaredMethod(methodName, argTypes);
        } else method = object.getClass().getDeclaredMethod(methodName);

        method.setAccessible(true);

        return args.length > 0 ?
                        methodName.equals("shape") || methodName.equals("importerURL") ?
                                method.invoke(object, args[0], args[1]) :
                                method.invoke(object, args[0]) : method.invoke(object);

    }

    @Test
    public void testGetProvider() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"), null);

        Provider provider = ProviderUtils.getByLabel("erg.kcl.ac.uk");

        assertEquals("erg.kcl.ac.uk", provider.getLabel());
        assertEquals("Environmental Research Group Kings College London",
                                                                            provider.getName());

    }

    @Test
    public void testGetAttributes() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"), null);

        Attribute attribute = AttributeUtils.getByProviderAndLabel("erg.kcl.ac.uk", AttributeUtils.nameToLabel("SiteCode"));

        assertEquals(33, laqnImporter.getAttributeSize());
        assertEquals(AttributeUtils.nameToLabel("SiteCode"), attribute.getLabel());
        assertEquals("Unique key", attribute.getDescription());

    }

    @Test
    public void testGetDatasource() throws Exception {
        List<String> datasources = laqnImporter.getDatasourceIds();

        assertEquals(1, datasources.size());
        assertEquals("airQualityControl", datasources.get(0));

    }

    @Test
    public void testGetSubjectType() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"), null);

        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel("erg.kcl.ac.uk",
                "airQualityControl");

        assertEquals("airQualityControl", subjectType.getLabel());
        assertEquals("Quantity of gases in air by Kings College London", subjectType.getName());
    }


    @Test
    public void testGetSubjects() throws Exception {

        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"), null);
        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        assertEquals("BG1", subjects.get(0).getLabel());
        assertEquals("Barking and Dagenham - Rush Green", subjects.get(0).getName());
        // Testing x coordinate (longitude)
        assertEquals(0.177891, subjects.get(0).getShape().getCoordinate().getOrdinate(0), 0.0001);
        // Testing y coordinate (latitude)
        assertEquals(51.563752, subjects.get(0).getShape().getCoordinate().getOrdinate(1), 0.0001);
    }

    @Test
    public void testGetFixedValue() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"), null);

        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        Attribute attribute = AttributeUtils.getByProviderAndLabel("erg.kcl.ac.uk", AttributeUtils.nameToLabel("SiteCode"));

        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subjects.get(0), attribute);

        assertEquals("BG1", fixedValue.getValue());

    }

    @Test
    public void testShape() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Geometry geometry =
                (Geometry) allowAccessToPrivateMethods("shape", "51.563752", "0.177891");

        assertEquals("Point", geometry.getGeometryType());
        assertEquals(4326, geometry.getSRID());

    }

    @Test
    public void testImportURL() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        String url = (String) allowAccessToPrivateMethods("importerURL", "London", "2010");

        assertEquals("http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/GroupName=London/Year=2010/json",
                      url);

    }

    @Test
    public void testTimedValue() throws Exception {

        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"), null);
        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(

                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        Attribute attribute = AttributeUtils.getByProviderAndLabel("erg.kcl.ac.uk",
                                                    "NO2 40 ug/m3 as an annual me");

        List<TimedValue> value = TimedValueUtils.getBySubjectAndAttribute(subjects.get(0), attribute);
        assertEquals(Double.toString(26.0), Double.toString(value.get(0).getValue()));
        assertEquals(LocalDateTime.parse("2010-12-31T23:59:59"), value.get(0).getId().getTimestamp());

    }

    @Test
    public void testTime() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        LocalDateTime time = (LocalDateTime) allowAccessToPrivateMethods("time", "2010");
        assertEquals(LocalDateTime.parse("2010-12-31T23:59:59"), time);
    }
}
