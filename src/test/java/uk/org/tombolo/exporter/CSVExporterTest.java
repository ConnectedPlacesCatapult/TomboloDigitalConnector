package uk.org.tombolo.exporter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CSVExporterTest {
	CSVExporter exporter = new CSVExporter();

	@Test
	public void testWrite() throws Exception{
		Writer writer = new StringWriter();

		exporter.write(writer, makeDatasetSpecification());
		writer.flush();

		assertEquals("E09000033", getFirstFeatureLabel(writer.toString()));
	}

	@Test
	public void tabulateGeographies() throws Exception {
		List<Geography> geographies = new ArrayList<Geography>();
		geographies.add(new Geography(
				new GeographyType("type_label", "type_name"),
				"label",
				"name",
				new GeometryFactory().createPoint(new Coordinate(0,0))
		));
		List<Object> geography = exporter.tabulateGeographies(geographies).get(0);

		assertEquals("type_label",  (String) geography.get(0));
		assertEquals("label",       (String) geography.get(1));
		assertEquals("name",        (String) geography.get(2));
		assertEquals("POINT (0 0)", (String) geography.get(3));

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

	private String getFirstFeatureLabel(String csvString) throws IOException {
		CSVParser parser = CSVParser.parse(csvString, CSVFormat.DEFAULT.withHeader());
		List<CSVRecord> records = parser.getRecords();
		return records.get(0).get("label");
	}
}