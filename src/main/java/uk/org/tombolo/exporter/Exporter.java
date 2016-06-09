package uk.org.tombolo.exporter;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.field.Field;

import java.io.Writer;
import java.util.List;

public interface Exporter {

	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception;

	void write(Writer writer, List<Subject> subjects, List<Field> fields) throws Exception;
}
