package uk.org.tombolo.lac;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.lac.LAQNImporter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * LAQNImporterTest file
 */
public class LAQNImporterTest extends AbstractTest {

    private LAQNImporter laqnImporter;
    private Config defaultConfig = new Config.Builder(0, "",
                            "/datacache/TomboloData/uk.lac/config.properties", "erg.kcl.ac.uk",
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
                Collections.singletonList("London"), Collections.singletonList("2010"));

        Provider provider = ProviderUtils.getByLabel("erg.kcl.ac.uk");

        assertEquals("erg.kcl.ac.uk", provider.getLabel());
        assertEquals("Environmental Research Group Kings College London",
                                                                            provider.getName());

    }

    @Test
    public void testGetAttributes() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"));

        Attribute attribute = AttributeUtils.getByProviderAndLabel("erg.kcl.ac.uk", "SiteCode");

        assertEquals(33, laqnImporter.getAttributeSize());
        assertEquals("SiteCode", attribute.getLabel());
        assertEquals("SiteCode", attribute.getName());
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
                Collections.singletonList("London"), Collections.singletonList("2010"));

        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel("erg.kcl.ac.uk",
                "airQualityControl");

        assertEquals("airQualityControl", subjectType.getLabel());
        assertEquals("Quantity of gases in air by Kings College London", subjectType.getName());
    }


    @Test
    public void testReadData() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        ArrayList<LinkedHashMap<String, List<String>>> data =
                (ArrayList<LinkedHashMap<String, List<String>>>) allowAccessToPrivateMethods("readData",
                        "http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/GroupName=London/Year=2010/json");

        assertEquals(new HashSet<>(Arrays.asList("@SiteCode", "@SiteName", "@SiteType", "@Latitude",
                                    "@Longitude", "@LatitudeWGS84", "@LongitudeWGS84", "@SiteLink",
                                    "@DataOwner", "@DataManager", "@SpeciesCode", "@SpeciesDescription",
                                    "@Year", "@ObjectiveName", "@Value", "@Achieved"
                                    )), data.get(0).keySet());

    }


    @Test
    public void testGetSubjects() throws Exception {

        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"));
        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        assertEquals("BG1", subjects.get(0).getLabel());
        assertEquals("Barking and Dagenham - Rush Green", subjects.get(0).getName());

    }

    @Test
    public void testGetFixedValue() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"));

        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        Attribute attribute = AttributeUtils.getByProviderAndLabel("erg.kcl.ac.uk", "SiteCode");

        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subjects.get(0), attribute);

        assertEquals("BG1", fixedValue.getValue());

    }

    @Test
    public void testShape() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Geometry geometry =
                (Geometry) allowAccessToPrivateMethods("shape", "51.563752", "0.177891");

        assertEquals("Point", geometry.getGeometryType());

    }

    @Test
    public void testImportURL() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        String url = (String) allowAccessToPrivateMethods("importerURL", "London", "2010");

        assertEquals("http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/GroupName=London/Year=2010/json",
                      url);

    }

    @Test
    public void testImportDatasource() throws Exception {
        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"));
        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"BG1");

        assertEquals(1, subjects.size());
        assertEquals("Barking and Dagenham - Rush Green", subjects.get(0).getName());

    }

    @Test
    public void testTimedValue() throws Exception {

        laqnImporter.importDatasource("airQualityControl",
                Collections.singletonList("London"), Collections.singletonList("2010"));
        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(

                                "erg.kcl.ac.uk","airQualityControl"),"%%");

        Attribute attribute = AttributeUtils.getByProviderAndLabel("erg.kcl.ac.uk",
                                                    "NO2 40 ug/m3 as an annual me");

        List<TimedValue> value = TimedValueUtils.getBySubjectAndAttribute(subjects.get(0), attribute);
        assertEquals(Double.toString(26.0), Double.toString(value.get(0).getValue()));

    }

    @Test
    public void testTime() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        LocalDateTime time = (LocalDateTime) allowAccessToPrivateMethods("time");
        assertEquals(LocalDateTime.now(), time);
    }
}
