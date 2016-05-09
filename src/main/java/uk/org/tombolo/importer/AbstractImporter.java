package uk.org.tombolo.importer;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.TimedValueUtils;

public abstract class AbstractImporter implements Importer {

	protected DownloadUtils downloadUtils = new DownloadUtils();
	protected TimedValueUtils timedValueUtils;

	public AbstractImporter(TimedValueUtils timedValueUtils) {
		this.timedValueUtils = timedValueUtils;
	}

	public void setDownloadUtils(DownloadUtils downloadUtils){
		this.downloadUtils = downloadUtils;
	}
	
	/**
	 * Loads the data-source identified by datasourceId into the underlying data store 
	 * 
	 * @param datasourceId
	 * @return the number of data values loaded
	 * @throws IOException
	 * @throws ParseException 
	 */
	public int importDatasource(String datasourceId) throws Exception {
		// Get the details for the data source
		Datasource datasource = getDatasource(datasourceId);
		return importDatasource(datasource);
	}

}
