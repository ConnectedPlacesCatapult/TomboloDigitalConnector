package uk.org.tombolo.importer.spacesyntax;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.ProcessingInstruction;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class SpaceSyntaxShapefileImporterTest extends AbstractTest {
    SpaceSyntaxShapefileImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new SpaceSyntaxShapefileImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
    }

    @Test
    public void getProvider() throws Exception {
        Provider provider = importer.getProvider();

        assertEquals("com.spacesyntax", provider.getLabel());
        assertEquals("Space Syntax", provider.getName());
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(0,datasources.size());
    }

    @Test
    public void getDatasource() throws Exception {
        String resourcePath = "datacache/TomboloData/com.spacesyntax/SSx_sample/SSx_sample.shp";
        ClassLoader classLoader = getClass().getClassLoader();
        String shapefilePath = classLoader.getResource(resourcePath).getPath();

        Datasource datasource = importer.getDatasource(shapefilePath);

        assertEquals("SSx_sample",datasource.getId());
        assertEquals("SSx_sample",datasource.getName());
        assertEquals("",datasource.getDescription());
        assertEquals(shapefilePath, datasource.getLocalDatafile());
        assertNull(datasource.getRemoteDatafile());

        assertEquals(61, datasource.getAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        String resourcePath = "datacache/TomboloData/com.spacesyntax/SSx_sample/SSx_sample.shp";
        ClassLoader classLoader = getClass().getClassLoader();
        String shapefilePath = classLoader.getResource(resourcePath).getPath();

        int importedCount = importer.importDatasource(shapefilePath);
        assertEquals(61*287, importedCount);

        Subject streetSegment = SubjectUtils.getSubjectByLabel("SSx_sample:4702");

        assertEquals("SSx_sample:4702", streetSegment.getName());
        assertEquals("SSxNode", streetSegment.getSubjectType().getLabel());
        assertEquals(0.089275, streetSegment.getShape().getCentroid().getX(), 1.0E-6);
        assertEquals(51.523725, streetSegment.getShape().getCentroid().getY(), 1.0E-6);

        // Test timed values
        Attribute angularCost = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "Angular_Co");
        List<TimedValue> angularCosts = TimedValueUtils.getBySubjectAndAttribute(streetSegment, angularCost);
        assertEquals(1, angularCosts.size());
        assertEquals(0.30472368, angularCosts.get(0).getValue(),1.0E-8);

        Attribute nc800 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "NC800");
        List<TimedValue> nc800s = TimedValueUtils.getBySubjectAndAttribute(streetSegment, nc800);
        assertEquals(1, nc800s.size());
        assertEquals(12.0, nc800s.get(0).getValue(), 0.1);
    }

}