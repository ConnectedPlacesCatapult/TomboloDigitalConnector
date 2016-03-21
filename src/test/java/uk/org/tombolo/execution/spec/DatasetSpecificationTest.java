package uk.org.tombolo.execution.spec;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class DatasetSpecificationTest {

	@Test
	public void testFromJsonFile() throws JsonSyntaxException, JsonIOException, FileNotFoundException{
		String resourcePath = "executions/tombolo/obesity_uk.json";
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		DatasetSpecification dsSpec = DatasetSpecification.fromJsonFile(file);
		
		assertEquals(1, dsSpec.getGeographySpecification().size());
		assertEquals(2, dsSpec.getDatasourceSpecification().size());
		assertEquals(3, dsSpec.getAttributeSpecification().size());
	}
}
