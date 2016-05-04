package uk.org.tombolo.exporter;

import org.junit.Test;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CSVExporterTest {
	CSVExporter exporter = new CSVExporter();

	@Test
	public void testWrite() throws Exception {
		Writer writer = new StringWriter();
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		geographySpecification.add(new GeographySpecification("localAuthority","E09%"));
		spec.setGeographySpecification(geographySpecification);

		exporter.write(writer, spec);
		writer.flush();

		assertEquals("mate we got some java", writer.toString());
	}
}