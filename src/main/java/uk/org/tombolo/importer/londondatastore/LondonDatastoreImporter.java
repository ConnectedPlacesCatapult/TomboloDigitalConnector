package uk.org.tombolo.importer.londondatastore;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ExcelImporter;
import uk.org.tombolo.importer.Importer;

public class LondonDatastoreImporter extends ExcelImporter implements Importer {
	public static final Provider PROVIDER = new Provider(
			"uk.gov.london",
			"London Datastore - Greater London Authority"
			);

	private static final String DATASOURCE_SPEC_DIR = "/datasources/uk/gov/london";
	private static final int TIMEDVALUE_BUFFER_SIZE = 1000;

	public LondonDatastoreImporter(){
		datasourceSpecDir = DATASOURCE_SPEC_DIR;
		timedValueBufferSize = TIMEDVALUE_BUFFER_SIZE;
	}

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}
}
