package uk.org.tombolo.exporter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CSVExporterTest {
	CSVExporter exporter = new CSVExporter();

	@Test
	public void testWrite() throws Exception {
		Writer writer = new StringWriter();
		exporter.write(writer, makeDatasetSpecification());
		writer.flush();

		CSVRecord record = getFirstRecord(writer.toString());

		assertEquals("E09000033", record.get("label"));
		assertEquals("Westminster", record.get("name"));
		assertNotNull(record.get("geometry"));
		assertEquals("Population density (per hectare) 2015", record.get("uk.gov.london_populationDensity_name"));
		assertEquals("London Datastore - Greater London Authority", record.get("uk.gov.london_populationDensity_provider"));
		assertEquals("109.36855714483839", record.get("uk.gov.london_populationDensity_latest_value"));
	}

	@Test
	public void testGetColumnNames() throws Exception {
		List<String> attributes = exporter.getColumnNames(makeAttributes());

		assertEquals("label", attributes.get(0));
		assertEquals("name", attributes.get(1));
		assertEquals("geometry", attributes.get(2));
		assertEquals("uk.gov.london_populationDensity_name", attributes.get(3));
		assertEquals("uk.gov.london_populationDensity_provider", attributes.get(4));
	}

	private DatasetSpecification makeDatasetSpecification() {
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		geographySpecification.add(new GeographySpecification("E09%", "localAuthority"));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification("uk.gov.london", "populationDensity"));
		spec.setGeographySpecification(geographySpecification);
		spec.setAttributeSpecification(attributeSpecification);
		return spec;
	}

	private List<Attribute> makeAttributes() {
		return new ArrayList<>(Arrays.asList(
				new Attribute(
						new Provider("uk.gov.london", "London Datastore - Greater London Authority"),
						"populationDensity",
						"Population density (per hectare) 2015",
						"description1",
						null
				)
		));
	}

	private CSVRecord getFirstRecord(String csvString) throws IOException {
		CSVParser parser = CSVParser.parse(csvString, CSVFormat.DEFAULT.withHeader());
		List<CSVRecord> records = parser.getRecords();

		return records.get(0);
	}
}