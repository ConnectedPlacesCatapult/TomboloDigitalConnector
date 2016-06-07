package uk.org.tombolo.execution.spec;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Attribute;

public class DatasetSpecificationTest extends AbstractTest {

	@Test
	public void testFromJsonFile() throws FileNotFoundException{
		DatasetSpecification dsSpec = makeDatasetSpecification();
		
		assertEquals(1, dsSpec.getSubjectSpecification().size());
		assertEquals(3, dsSpec.getDatasourceSpecification().size());
		assertEquals(3, dsSpec.getAttributeSpecification().size());
	}

	@Test
	public void testGetTransformSpecification() throws Exception {
		DatasetSpecification dsSpec = makeDatasetSpecification();

		List<TransformSpecification> transformSpec = new ArrayList<TransformSpecification>();
		transformSpec.add(new TransformSpecification(new ArrayList<>(), new Attribute(), "className"));

		dsSpec.setTransformSpecification(transformSpec);
		assertSame(transformSpec, dsSpec.getTransformSpecification());

	}

	private DatasetSpecification makeDatasetSpecification() throws FileNotFoundException {
		String resourcePath = "executions/test_dataset_spec.json";
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		return DatasetSpecification.fromJsonFile(file);
	}
}
