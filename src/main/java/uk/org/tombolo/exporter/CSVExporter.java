package uk.org.tombolo.exporter;

import java.io.Writer;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.utils.GeographyTypeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class CSVExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader());
		printer.printRecord(Arrays.asList("type_label", "label", "name", "geometry"));
		printer.printRecords(
				tabulateGeographies(getGeographies(datasetSpecification))
		);
	}

	public List<List<Object>> tabulateGeographies(List<Geography> geographies) {
		List<List<Object>> table = new ArrayList<List<Object>>();
		for(Geography geography : geographies) {
			List<Object> row = new ArrayList<Object>();
			row.add(geography.getGeographyType().getLabel());
			row.add(geography.getLabel());
			row.add(geography.getName());
			row.add(geography.getShape().toString());

			table.add(row);
		}

		return table;
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
