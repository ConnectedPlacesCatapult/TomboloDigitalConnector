package uk.org.tombolo.lac;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.lac.LAQNConfig;
import uk.org.tombolo.importer.lac.LAQNImporter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public void setUp() throws IOException {

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
            Class[] argTypes = methodName.equals("shape") ? new Class[] { String.class, String.class } :
                                new Class[] { String.class };
            method = object.getClass().getDeclaredMethod(methodName, argTypes);
        } else method = object.getClass().getDeclaredMethod(methodName);

        method.setAccessible(true);

        return args.length > 0 ?
                        methodName.equals("shape") ? method.invoke(object, args[0], args[1]) :
                                method.invoke(object, args[0]) : method.invoke(object);

    }

    @Test
    public void testGetProvider() {

        assertEquals("erg.kcl.ac.uk", laqnImporter.getProvider().getLabel());
        assertEquals("Environmental Research Group Kings College London",
                                                                            laqnImporter.getProvider().getName());

    }

    @Test
    public void testGetDatasource() throws Exception {

        assertEquals("airQualityControl", laqnImporter.getDatasource("").getId());
        assertEquals("airQualityControl", laqnImporter.getDatasource("").getName());
        assertEquals("Quantity of gases in air by Kings College London",
                                                        laqnImporter.getDatasource("").getDescription());

    }

    @Test
    public void testGetFixedAttributes() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        ArrayList<Attribute> attributes =
                (ArrayList<Attribute>) allowAccessToPrivateMethods("getFixedAttributes");

        assertEquals("SiteCode", attributes.get(0).getLabel());

    }

    @Test
    public void testReadData() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        ArrayList<LinkedHashMap<String, List<String>>> data =
                (ArrayList<LinkedHashMap<String, List<String>>>) allowAccessToPrivateMethods("readData");

        assertEquals(new HashSet<>(Arrays.asList("@SiteCode", "@SiteName", "@SiteType", "@Latitude",
                                    "@Longitude", "@LatitudeWGS84", "@LongitudeWGS84", "@SiteLink",
                                    "@DataOwner", "@DataManager", "@SpeciesCode", "@SpeciesDescription",
                                    "@Year", "@ObjectiveName", "@Value", "@Achieved"
                                    )), data.get(0).keySet());

    }

    @Test
    public void testGetSubjectType() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        SubjectType subjectType =
                (SubjectType) allowAccessToPrivateMethods("getSubjectType");

        assertEquals("airQualityControl", subjectType.getLabel());
        assertEquals("Quantity of gases in air by Kings College London", subjectType.getName());
    }

    @Test
    public void testGetSubjects() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        ArrayList<Subject> subjects =
                (ArrayList<Subject>) allowAccessToPrivateMethods("getSubjects");

        assertEquals("BG1", subjects.get(0).getLabel());
        assertEquals("Barking and Dagenham - Rush Green", subjects.get(0).getName());

    }

    @Test
    public void testGetFixedValue() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        ArrayList<FixedValue> fixedValues =
                (ArrayList<FixedValue>) allowAccessToPrivateMethods("getFixedValue");

        assertEquals("Barking and Dagenham - Rush Green", fixedValues.get(1).getValue());
        assertEquals("BG1", fixedValues.get(1).getId().getSubject().getLabel());
        assertEquals("SiteName", fixedValues.get(1).getId().getAttribute().getLabel());

    }

    @Test
    public void testShape() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Geometry geometry =
                (Geometry) allowAccessToPrivateMethods("shape", "51.563752", "0.177891");

        assertEquals("Point", geometry.getGeometryType());

    }

    @Test
    public void testConfig() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {

        LAQNConfig config =
                (LAQNConfig) allowAccessToPrivateMethods("config", "/datacache/TomboloData/uk.lac/config.properties");

        assertEquals("2010", config.getYear());
        assertEquals("London", config.getArea());

    }

    @Test
    public void testImportURL() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        String url = (String) allowAccessToPrivateMethods("importerURL");

        assertEquals("http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/GroupName=London/Year=2010/json",
                      url);

    }

    @Test
    public void testImportDatasource() throws Exception {

        laqnImporter.importDatasource("airQualityControl");

        List<Subject> subjects =
                SubjectUtils.getSubjectByTypeAndLabelPattern(
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                                "erg.kcl.ac.uk","airQualityControl"),"BG1");

        assertEquals(9, subjects.size());
        assertEquals("", subjects.get(0).getName());

    }
}
