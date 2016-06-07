package uk.org.tombolo.exporter;

import java.io.Writer;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.execution.spec.DatasetSpecification;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		List<Attribute> attributes = AttributeUtils.getAttributeBySpecification(datasetSpecification);
		List<String> columnNames = getColumnNames(attributes);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
		printer.printRecord(columnNames);

		for (Subject geography : GeographyUtils.getGeographyBySpecification(datasetSpecification)) {
			printer.printRecord(
					tabulateGeographyMap(columnNames,
							flattenGeography(attributes, geography)));
		}
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

	// Take the geography/attributes structure and convert it to a key-value map
	private Map<String, Object> flattenGeography(List<Attribute> attributes, Subject geography) {
		Map<String, Object> row = new HashMap<>();

		row.put("label", geography.getLabel());
		row.put("name", geography.getName());
		row.put("geometry", geography.getShape().toString());

		for (Attribute attribute : attributes) {
			row.putAll(getAttributeProperty(geography, attribute));
		}

		return row;
	}
	
	private List<String> tabulateGeographyMap(List<String> attributes, Map<String, Object> map) {
		List<String> listRow = new ArrayList<String>();

		for (String attribute : attributes) {
			listRow.add((String) map.getOrDefault(attribute, ""));
		}

		return listRow;
	}

	private Map<String, Object> getAttributeProperty(Subject geography, Attribute attribute) {
		Map<String, Object> property = new HashMap<>();

		property.put(getAttributePropertyName(attribute, "name"), attribute.getName());
		property.put(getAttributePropertyName(attribute, "provider"), attribute.getProvider().getName());

		TimedValueUtils timedValueUtils = new TimedValueUtils();
		timedValueUtils.getLatestByGeographyAndAttribute(geography, attribute).ifPresent(
				timedValue -> property.put(
						getAttributePropertyName(attribute, "latest_value"),
						timedValue.getValue().toString()));

		return property;
	}

	private String getAttributePropertyName(Attribute attribute, String property) {
		return String.join("_", attribute.uniqueLabel(), property);
	}
}
