package uk.org.tombolo.exporter;

import java.io.Writer;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		List<Attribute> attributes = AttributeUtils.getAttributeBySpecification(datasetSpecification);
		List<String> columnNames = getColumnNames(attributes);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
		printer.printRecord(columnNames);
		printer.printRecords(
				tabulateGeographyMap(columnNames,
						flattenGeographies(attributes,
								GeographyUtils.getGeographyBySpecification(datasetSpecification))));
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
	private List<Map<String, Object>> flattenGeographies(List<Attribute> attributes, List<Geography> geographies) {
		List<Map<String, Object>> table = new ArrayList<>();

		for(Geography geography : geographies) {
			Map<String, Object> row = new HashMap<>();

			row.put("label", geography.getLabel());
			row.put("name", geography.getName());
			row.put("geometry", geography.getShape().toString());

			for (Attribute attribute : attributes) {
				row.putAll(getAttributeProperty(geography, attribute));
			}

			table.add(row);
		}

		return table;
	}
	
	private List<List<String>> tabulateGeographyMap(List<String> attributes, List<Map<String, Object>> mapTable) {
		List<List<String>> listTable = new ArrayList<>();
		for (Map <String, Object> mapRow : mapTable) {
			List<String> listRow = new ArrayList<String>();

			for (String attribute : attributes) {
				listRow.add((String) mapRow.getOrDefault(attribute, ""));
			}

			listTable.add(listRow);
		}

		return listTable;
	}

	private Map<String, Object> getAttributeProperty(Geography geography, Attribute attribute) {
		Map<String, Object> property = new HashMap<>();

		property.put(getAttributePropertyName(attribute, "name"), attribute.getName());
		property.put(getAttributePropertyName(attribute, "provider"), attribute.getProvider().getName());

		TimedValueUtils.getLatestByGeographyAndAttribute(geography, attribute).ifPresent(
				timedValue -> property.put(
						getAttributePropertyName(attribute, "latest_value"),
						timedValue.getValue().toString()));

		return property;
	}

	private String getAttributePropertyName(Attribute attribute, String property) {
		return String.join("_", attribute.uniqueLabel(), property);
	}
}
