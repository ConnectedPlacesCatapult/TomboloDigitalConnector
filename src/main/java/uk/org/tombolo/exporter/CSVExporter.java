package uk.org.tombolo.exporter;

import java.io.Writer;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyTypeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		List<Attribute> attributes = getDatasetSpecificationAttributes(datasetSpecification);
		List<String> columnNames = getColumnNames(attributes);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
		printer.printRecord(columnNames);
		printer.printRecords(
				tabulateGeographyMap(columnNames,
						flattenGeographies(attributes,
								getGeographies(datasetSpecification))));
	}

	public List<String> getColumnNames(List<Attribute> attributes) {
		List<String> columnNames = new ArrayList<>(Arrays.asList("label", "name", "geometry"));

		for (Attribute attribute : attributes) {
			columnNames.add(getAttributePropertyName(attribute, "name"));
			columnNames.add(getAttributePropertyName(attribute, "provider"));
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

		return property;
	}

	private List<Geography> getGeographies(DatasetSpecification datasetSpecification) {
		List<Geography> geographies = new ArrayList<>();

		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
			List<Geography> geographyList = GeographyUtils
					.getGeographyByTypeAndLabelPattern(geographyType, geographySpecification.getLabelPattern());
			geographies.addAll(geographyList);
		}

		return geographies;
	}

	private String getAttributePropertyName(Attribute attribute, String property) {
		return String.join("_", attribute.uniqueLabel(), property);
	}

	private List<Attribute> getDatasetSpecificationAttributes(DatasetSpecification datasetSpecification) {
		List<Attribute> list = new ArrayList<>();

		List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
		for (AttributeSpecification attributeSpec : attributeSpecs) {
			Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
			Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());
			list.add(attribute);
		}

		return list;
	}
}
