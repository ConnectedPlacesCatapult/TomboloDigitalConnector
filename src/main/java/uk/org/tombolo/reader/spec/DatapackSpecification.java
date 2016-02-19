package uk.org.tombolo.reader.spec;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import uk.org.tombolo.datacatalogue.DatasourceSpecification;

public class DatapackSpecification {

	EntitySpecification entitySpecification;
	Map<String, DatasourceSpecification> idToDatasourceSpecifications;
	List<SeriesSpecification> seriesSpecifications;
	
	public static DatapackSpecification fromJsonFile(File file) throws IOException{
		Gson gson = new Gson();
		Reader reader = new FileReader(file);
		return gson.fromJson(reader, DatapackSpecification.class);
	}

	public EntitySpecification getEntitySpecification() {
		return entitySpecification;
	}

	public List<SeriesSpecification> getSeriesSpecifications() {
		return seriesSpecifications;
	}
	
	public DatasourceSpecification getDatasourceSpecificationById(String id){
		return idToDatasourceSpecifications.get(id);
	}
}
