package uk.org.tombolo.recipe;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DatasetRecipeTest extends AbstractTest {

	@Test
	public void testFromJsonFile() throws IOException {
		DatasetRecipe dsSpec = makeDatasetSpecification();
		
		assertEquals(1, dsSpec.getSubjects().size());
		assertEquals(3, dsSpec.getDatasources().size());
		assertEquals(1, dsSpec.getFields().size());
	}

	@Test
	public void testGetFieldSpecification() throws Exception {
		DatasetRecipe dsSpec = makeDatasetSpecification();
		assertEquals("fractionObese", dsSpec.getFields().get(0).toField().getLabel());
		assertEquals("uk.org.tombolo.field.value.LatestValueField", dsSpec.getFields().get(0).toField().getClass().getCanonicalName());
	}

	private DatasetRecipe makeDatasetSpecification() throws IOException {
		String resourcePath = "executions/test_dataset_spec.json";
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		return RecipeDeserializer.fromJsonFile(file, DataExportRecipe.class).getDataset();
	}
}
