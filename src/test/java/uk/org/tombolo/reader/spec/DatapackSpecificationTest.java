package uk.org.tombolo.reader.spec;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import uk.org.tombolo.reader.spec.DatapackSpecification;

public class DatapackSpecificationTest {

	
	@Test
	public void testFromJsonFile() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String resourcePath = "experiments/organicity_boroughs.json";		
		File file = new File(classLoader.getResource(resourcePath).getFile());
		
		DatapackSpecification spec = DatapackSpecification.fromJsonFile(file);
		
		assertEquals(4, spec.idToDatasourceSpecifications.keySet().size());
		//FIXME: Add more tests
	}
}
