package uk.org.tombolo.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.FieldWithProvider;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class CSVExporter implements Exporter {
	private Logger log = LoggerFactory.getLogger(CSVExporter.class);

	@Override
	public void write(Writer writer, List<Subject> subjects, List<Field> fields) throws IOException {
		List<String> columnNames = getColumnNames(fields);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
		printer.printRecord(columnNames);

		for (Subject subject : subjects) {
			printer.printRecord(
					tabulateSubjectMap(columnNames,
							flattenSubject(fields, subject)));
		}
	}

	public List<String> getColumnNames(List<Field> fields) {
		List<String> columnNames = new ArrayList<>(Arrays.asList("label", "name", "geometry"));

		for (Field field : fields) {
			columnNames.add(getFieldPropertyName(field, "name"));
			if (field instanceof FieldWithProvider) {
				columnNames.add(getFieldPropertyName(field, "provider"));
			}
			columnNames.add(getFieldPropertyName(field, "latest_value"));
		}

		return columnNames;
	}

	// Take the subject/fields structure and convert it to a key-value map
	private Map<String, Object> flattenSubject(List<Field> fields, Subject subject) {
		Map<String, Object> row = new HashMap<>();

		row.put("label", subject.getLabel());
		row.put("name", subject.getName());
		row.put("geometry", subject.getShape().toString());

		for (Field field : fields) {
			row.putAll(getAttributeProperty(subject, field));
		}

		return row;
	}
	
	private List<String> tabulateSubjectMap(List<String> attributes, Map<String, Object> map) {
		List<String> listRow = new ArrayList<String>();

		for (String attribute : attributes) {
			listRow.add((String) map.getOrDefault(attribute, ""));
		}

		return listRow;
	}

	private Map<String, Object> getAttributeProperty(Subject subject, Field field) {
		Map<String, Object> property = new HashMap<>();

		property.put(getFieldPropertyName(field, "name"), field.getHumanReadableName());
		if (field instanceof FieldWithProvider) {
			property.put(getFieldPropertyName(field, "provider"), ((FieldWithProvider) field).getProvider().getName());
		} else {
			property.put(getFieldPropertyName(field, "provider"), null);
		}

		if (field instanceof SingleValueField) {
			try {
				property.put(getFieldPropertyName(field, "latest_value"), ((SingleValueField) field).valueForSubject(subject));
			} catch (IncomputableFieldException e) {
				log.warn("Could not compute Field %s for Subject %s, reason: %s", field.getLabel(), subject.getLabel(), e.getMessage());
				property.put(getFieldPropertyName(field, "latest_value"), null);
			}
		} else {
			throw new IllegalArgumentException(String.format("Field %s cannot return a single value", field.getLabel()));
		}

		return property;
	}

	private String getFieldPropertyName(Field field, String property) {
		return String.join("_", field.getLabel(), property);
	}
}
