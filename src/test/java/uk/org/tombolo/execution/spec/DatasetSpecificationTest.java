package uk.org.tombolo.execution.spec;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DatasetSpecificationTest extends AbstractTest {

	@Test
	public void testFromJsonFile() throws IOException {
		DatasetSpecification dsSpec = makeDatasetSpecification();
		
		assertEquals(1, dsSpec.getSubjectSpecification().size());
		assertEquals(3, dsSpec.getDatasourceSpecification().size());
		assertEquals(1, dsSpec.getFieldSpecification().size());
	}

	@Test
	public void testGetTransformSpecification() throws Exception {
		DatasetSpecification dsSpec = makeDatasetSpecification();

		List<TransformSpecification> transformSpec = new ArrayList<TransformSpecification>();
		transformSpec.add(new TransformSpecification(new ArrayList<>(), new Attribute(), "className"));

		dsSpec.setTransformSpecification(transformSpec);
		assertSame(transformSpec, dsSpec.getTransformSpecification());
	}

	@Test
	public void testGetFieldSpecification() throws Exception {
		DatasetSpecification dsSpec = makeDatasetSpecification();
		assertEquals("fractionObese", dsSpec.getFieldSpecification().get(0).toField().getLabel());
		assertEquals("uk.org.tombolo.field.LatestValueField", dsSpec.getFieldSpecification().get(0).toField().getClass().getCanonicalName());
	}

	private DatasetSpecification makeDatasetSpecification() throws IOException {
		String resourcePath = "executions/test_dataset_spec.json";
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		return DataExportSpecification.fromJsonFile(file).getDatasetSpecification();
	}
}
