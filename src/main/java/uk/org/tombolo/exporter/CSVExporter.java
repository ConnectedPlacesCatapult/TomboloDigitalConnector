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
		List<String> attributes = getAllAttributes(datasetSpecification);

		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
		printer.printRecord(attributes);
		printer.printRecords(
				tabulateGeographyMap(attributes,
						lineariseGeographies(datasetSpecification,
								getGeographies(datasetSpecification))));

	}

	public List<Map<String, Object>> lineariseGeographies(DatasetSpecification datasetSpecification, List<Geography> geographies) {
		List<Map<String, Object>> table = new ArrayList<Map<String, Object>>();

		for(Geography geography : geographies) {
			Map<String, Object> row = new HashMap<String, Object>();

			row.put("label", geography.getLabel());
			row.put("name", geography.getName());
			row.put("geometry", geography.getShape().toString());

			List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
			for (AttributeSpecification attributeSpec : attributeSpecs) {
				Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
				Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());

				row.putAll(getAttributeProperty(geography, attribute, attributeSpec));
			}

			table.add(row);
		}

		return table;
	}

	public List<String> getAllAttributes(DatasetSpecification datasetSpecification) {
		List<String> attributes = new ArrayList<String>(Arrays.asList("label", "name", "geometry"));

		List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
		for (AttributeSpecification attributeSpec : attributeSpecs) {
			Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
			Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());

			String prefix = "attribute_" + attribute.getId() + "_";
			attributes.add(prefix + "name");
			attributes.add(prefix + "provider");
		}

		return attributes;
	}

	private List<List<String>> tabulateGeographyMap(List<String> attributes, List<Map<String, Object>> mapTable) {
		List<List<String>> listTable = new ArrayList<List<String>>();
		for (Map <String, Object> mapRow : mapTable) {
			List<String> listRow = new ArrayList<String>();

			for (String attribute : attributes) {
				listRow.add((String) mapRow.getOrDefault(attribute, ""));
			}

			listTable.add(listRow);
		}

		return listTable;
	}

	private Map<String, Object> getAttributeProperty(Geography geography, Attribute attribute, AttributeSpecification attributeSpec) {
		Map<String, Object> property = new HashMap<String, Object>();

		String prefix = "attribute_" + attribute.getId() + "_";
		property.put(prefix + "name", attribute.getName());
		property.put(prefix + "provider", attribute.getProvider().getName());

		return property;
	}

	private List<Geography> getGeographies(DatasetSpecification datasetSpecification) {
		List<Geography> geographies = new ArrayList<Geography>();

		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
			List<Geography> geographyList = GeographyUtils
					.getGeographyByTypeAndLabelPattern(geographyType, geographySpecification.getLabelPattern());
			geographies.addAll(geographyList);
		}

		return geographies;
	}

}
