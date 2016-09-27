package uk.org.tombolo.importer.phe;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.utils.excel.ExcelImporter;
import uk.org.tombolo.importer.Importer;

import static uk.org.tombolo.importer.phe.AbstractPheImporter.PROVIDER;

public class PheNooImporter extends ExcelImporter implements Importer {
	// FIXME: Reimplement to extend AbstractPheImporter and use xlsx tools

	private static final String DATASOURCE_SPEC_DIR = "/datasources/uk/gov/phe";	
	private static final int TIMEDVALUE_BUFFER_SIZE = 1000;
	
	public PheNooImporter(){
		datasourceSpecDir = DATASOURCE_SPEC_DIR;
		timedValueBufferSize = TIMEDVALUE_BUFFER_SIZE;
	}

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}

}
