package uk.org.tombolo.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class CSVExporter implements Exporter {
	private Logger log = LoggerFactory.getLogger(CSVExporter.class);
	private Boolean timeStamp;

	@Override
	public void write(Writer writer, List<Subject> subjects, List<Field> fields, Boolean timeStamp) throws IOException {
		this.timeStamp = null == timeStamp ? true : timeStamp;
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

		fields.stream().map(Field::getLabel).forEach(columnNames::add);

		return columnNames;
	}

	// Take the subject/fields structure and convert it to a key-value map
	private Map<String, Object> flattenSubject(List<Field> fields, Subject subject) {
		Map<String, Object> row = new HashMap<>();

		row.put("label", subject.getLabel());
		row.put("name", subject.getName());
		row.put("geometry", subject.getShape().toString());

		fields.stream().map(field -> getAttributeProperty(subject, field)).forEach(row::putAll);

		return row;
	}
	
	private List<String> tabulateSubjectMap(List<String> attributes, Map<String, Object> map) {
		List<String> listRow =
						 attributes.stream().map(attribute -> (String) map.getOrDefault(attribute, ""))
						.collect(Collectors.toList());

		return listRow;
	}

	private Map<String, Object> getAttributeProperty(Subject subject, Field field) {
		Map<String, Object> property = new HashMap<>();

		if (field instanceof SingleValueField) {
			try {
				property.put(field.getLabel(), ((SingleValueField) field).valueForSubject(subject, timeStamp));
			} catch (IncomputableFieldException e) {
				log.warn("\u001b[0;33m" + "Could not compute Field {} for Subject {}, reason: {}" + "\u001b[m",
						field.getLabel(), subject.getLabel(), e.getMessage());
				property.put(field.getLabel(), null);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not compute Field %s for Subject %s" +
						"(%s), reason: %s", field.getLabel(), subject.getLabel(), subject.getId(), e.getMessage()));
			}
		} else {
			throw new IllegalArgumentException(String.format("Field %s cannot return a single value", field.getLabel()));
		}

		return property;
	}
}
