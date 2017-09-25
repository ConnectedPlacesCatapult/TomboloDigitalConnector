package uk.org.tombolo.exporter;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;

import java.io.Writer;
import java.util.List;

public interface Exporter {
	void write(Writer writer, List<Subject> subjects, List<Field> fields, Boolean timeStamp) throws Exception;
}
