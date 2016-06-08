package uk.org.tombolo.exporter;

import uk.org.tombolo.execution.spec.DatasetSpecification;

import java.io.Writer;

public interface Exporter {

	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception;
}
