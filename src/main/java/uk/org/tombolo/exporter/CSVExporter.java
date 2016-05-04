package uk.org.tombolo.exporter;

import java.io.Writer;

import uk.org.tombolo.execution.spec.DatasetSpecification;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		writer.write("mate we got some java");
	}

}
