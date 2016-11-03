package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LondonDatastoreImporterLsoaAtlas extends AbstractTest {
	private static final String DATASOURCE_ID = "lsoa-atlas";
	LondonDatastoreImporter importer;

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E01000001");
	}

	@Before
	public void before(){
		importer = new LondonDatastoreImporter();
		mockDownloadUtils(importer);
	}
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getTimedValueAttributes();
		assertEquals(3, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		
		assertEquals(4, datapoints);
	}
}
