package uk.org.tombolo.importer.phe;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PheNooImporterTest extends AbstractTest {
	private static final String DATASOURCE_ID = "BMI_categories_2012-2014";
	PheNooImporter importer = new PheNooImporter();

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Before
	public void setDownloadUtils() {
		mockDownloadUtils(importer);
	}
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getAttributes();
		assertEquals(5, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		
		//FIXME: Find a way to match Gateshead etc.
		assertEquals(5, datapoints);
	}
}
