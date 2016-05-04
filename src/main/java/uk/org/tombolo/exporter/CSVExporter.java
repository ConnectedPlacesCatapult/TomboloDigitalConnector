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

import javax.json.JsonValue;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader());
		printer.printRecord(Arrays.asList("label", "name", "geometry", "attr_name", "attr_provider"));
		printer.printRecords(
				tabulateGeographies(getGeographies(datasetSpecification), datasetSpecification)
		);
	}

	public List<List<Object>> tabulateGeographies(List<Geography> geographies, DatasetSpecification datasetSpecification) {
		List<List<Object>> table = new ArrayList<List<Object>>();

		for(Geography geography : geographies) {
			List<Object> row = new ArrayList<Object>();

			row.add(geography.getLabel());
			row.add(geography.getName());
			row.add(geography.getShape().toString());

			List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
			for (AttributeSpecification attributeSpec : attributeSpecs) {
				Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
				Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());

				row.addAll(getAttributeProperty(geography, attribute, attributeSpec));
			}

			table.add(row);
		}

		return table;
	}

	private List<Object> getAttributeProperty(Geography geography, Attribute attribute, AttributeSpecification attributeSpec) {
		List<Object> property = new ArrayList<Object>();

		property.add(attribute.getName());
		property.add(attribute.getProvider().getName());

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
