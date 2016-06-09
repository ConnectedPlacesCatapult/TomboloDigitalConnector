package uk.org.tombolo.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.field.Field;

import java.io.Writer;
import java.util.*;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		List<Attribute> attributes = AttributeUtils.getAttributeBySpecification(datasetSpecification);
		List<String> columnNames = getColumnNames(attributes);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
		printer.printRecord(columnNames);

		for (Subject subject : SubjectUtils.getSubjectBySpecification(datasetSpecification)) {
			printer.printRecord(
					tabulateSubjectMap(columnNames,
							flattenSubject(attributes, subject)));
		}
	}

	@Override
	public void write(Writer writer, List<Subject> subjects, List<Field> fields) {
		// TODO: make work
	}

	public List<String> getColumnNames(List<Attribute> attributes) {
		List<String> columnNames = new ArrayList<>(Arrays.asList("label", "name", "geometry"));

		for (Attribute attribute : attributes) {
			columnNames.add(getAttributePropertyName(attribute, "name"));
			columnNames.add(getAttributePropertyName(attribute, "provider"));
			columnNames.add(getAttributePropertyName(attribute, "latest_value"));
		}

		return columnNames;
	}

	// Take the subject/attributes structure and convert it to a key-value map
	private Map<String, Object> flattenSubject(List<Attribute> attributes, Subject subject) {
		Map<String, Object> row = new HashMap<>();

		row.put("label", subject.getLabel());
		row.put("name", subject.getName());
		row.put("geometry", subject.getShape().toString());

		for (Attribute attribute : attributes) {
			row.putAll(getAttributeProperty(subject, attribute));
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

	private Map<String, Object> getAttributeProperty(Subject subject, Attribute attribute) {
		Map<String, Object> property = new HashMap<>();

		property.put(getAttributePropertyName(attribute, "name"), attribute.getName());
		property.put(getAttributePropertyName(attribute, "provider"), attribute.getProvider().getName());

		TimedValueUtils timedValueUtils = new TimedValueUtils();
		timedValueUtils.getLatestBySubjectAndAttribute(subject, attribute).ifPresent(
				timedValue -> property.put(
						getAttributePropertyName(attribute, "latest_value"),
						timedValue.getValue().toString()));

		return property;
	}

	private String getAttributePropertyName(Attribute attribute, String property) {
		return String.join("_", attribute.uniqueLabel(), property);
	}
}
