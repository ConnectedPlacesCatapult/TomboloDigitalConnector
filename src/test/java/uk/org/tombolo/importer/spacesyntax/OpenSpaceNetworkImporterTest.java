package uk.org.tombolo.importer.spacesyntax;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpenSpaceNetworkImporterTest extends AbstractTest {
    OpenSpaceNetworkImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new OpenSpaceNetworkImporter();
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
        Datasource datasource = importer.getDatasource("Exeter_segment_model");

        assertEquals("Exeter_segment_model",datasource.getId());
        assertEquals("Exeter_segment_model",datasource.getName());
        assertEquals("",datasource.getDescription());
        assertEquals("osn/Exeter_segment_model.zip", datasource.getLocalDatafile());
        assertNull(datasource.getRemoteDatafile());

        assertEquals(3, datasource.getTimedValueAttributes().size());
        assertEquals(3, datasource.getFixedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        int importedCount = importer.importDatasource("Exeter_segment_model");
        //assertEquals(6*12319, importedCount);

        Subject streetSegment = SubjectUtils.getSubjectByLabel("Exeter_segment_model:6632");

        assertEquals("Exeter_segment_model:6632", streetSegment.getName());
        assertEquals("SSxNode", streetSegment.getSubjectType().getLabel());
        assertEquals(-3.537614, streetSegment.getShape().getCentroid().getX(), 1.0E-6);
        assertEquals(50.720291, streetSegment.getShape().getCentroid().getY(), 1.0E-6);

        // Test fixed values
        Attribute streetClass = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "class");
        FixedValue streetClassValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, streetClass);
        assertEquals("Unclassified", streetClassValue.getValue());

        Attribute streetName = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "street_nam");
        FixedValue streetNameValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, streetName);
        assertEquals("Tudor Street", streetNameValue.getValue());

        Attribute networkId = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "id_network");
        FixedValue networkIdValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, networkId);
        assertEquals("2090", networkIdValue.getValue());

        // Test timed values
        Attribute angularCost = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "angular_co");
        List<TimedValue> angularCosts = TimedValueUtils.getBySubjectAndAttribute(streetSegment, angularCost);
        // FIXME: Enable when SSx adds angular cost to the graph
        assertEquals(0, angularCosts.size());

        Attribute customCost = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "custom_cos");
        List<TimedValue> customCosts = TimedValueUtils.getBySubjectAndAttribute(streetSegment, customCost);
        // FIXME: Enable when SSx adds angular cost to the graph
        assertEquals(0, customCosts.size());

        Attribute metricCost = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "metric_cos");
        List<TimedValue> metricCosts = TimedValueUtils.getBySubjectAndAttribute(streetSegment, metricCost);
        assertEquals(1, metricCosts.size());
        assertEquals("2016-10-11T17:44:59", metricCosts.get(0).getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
        assertEquals(63, metricCosts.get(0).getValue(), 0.1d);
    }

}