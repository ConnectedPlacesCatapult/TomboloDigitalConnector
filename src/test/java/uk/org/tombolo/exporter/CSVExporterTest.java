package uk.org.tombolo.exporter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
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
import static uk.org.tombolo.execution.spec.GeographySpecification.*;

public class CSVExporterTest extends AbstractTest {
	CSVExporter exporter = new CSVExporter();

	@Test
	public void testWrite() throws Exception {
		Writer writer = new StringWriter();
		exporter.write(writer, makeDatasetSpecification("E09%", "localAuthority", "uk.gov.london", "populationDensity"));
		writer.flush();

		CSVRecord record = getRecords(writer.toString()).get(0);

		assertNotNull(record.get("label"));
		assertNotNull(record.get("name"));
		assertNotNull(record.get("geometry"));
		// FIXME: This keeps changing between 'Population Density' and 'Population density (per hectare) 2015'. Investigate
//		assertEquals("Population density (per hectare) 2015", record.get("uk.gov.london_populationDensity_name"));
		assertEquals("London Datastore - Greater London Authority", record.get("uk.gov.london_populationDensity_provider"));
		assertNotNull(record.get("uk.gov.london_populationDensity_latest_value"));
		assertEquals(6, record.size());
	}

	@Test
	public void testWriteWithInvalidProperty() throws Exception {
		Writer writer = new StringWriter();
		exporter.write(writer, makeDatasetSpecification("E09%", "localAuthority", "uk.gov.london", "badName"));
		writer.flush();

		CSVRecord record = getRecords(writer.toString()).get(0);

		assertNotNull(record.get("label"));
		assertNotNull(record.get("name"));
		assertNotNull(record.get("geometry"));
		assertEquals(3, record.size());
	}

	@Test
	public void testWriteWithInvalidGeography() throws Exception {
		Writer writer = new StringWriter();
		exporter.write(writer, makeDatasetSpecification("E09%", "badGeography", "uk.gov.london", "populationDensity"));
		writer.flush();

		assertTrue(getRecords(writer.toString()).isEmpty());
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

	private DatasetSpecification makeDatasetSpecification(String geographyLabelPattern, String geographyType, String attributeProvider, String attributeName) {
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		List<GeographyMatcher> matchers = Arrays.asList(new GeographyMatcher("label", geographyLabelPattern));
		geographySpecification.add(new GeographySpecification(matchers, geographyType));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification(attributeProvider, attributeName));
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

	private List<CSVRecord> getRecords(String csvString) throws IOException {
		CSVParser parser = CSVParser.parse(csvString, CSVFormat.DEFAULT.withHeader());
		return parser.getRecords();
	}
}