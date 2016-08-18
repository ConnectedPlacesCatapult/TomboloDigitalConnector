package uk.org.tombolo.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.LatestValueField;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CSVExporterTest extends AbstractTest {
	CSVExporter exporter = new CSVExporter();

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testWrite() throws Exception {
		Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
		TestFactory.makeTimedValue("E09000001", attribute, TestFactory.TIMESTAMP, 100d);

		Writer writer = new StringWriter();
		exporter.write(writer, makeSubjects(), makeFields("default_provider_label", "attr_label"));
		CSVRecord record = getRecords(writer.toString()).get(0);

		assertEquals("E09000001", record.get("label"));
		assertEquals("City of London", record.get("name"));
		assertNotNull(record.get("geometry"));
		assertEquals("attr_label_name", record.get("attr_label_name"));
		assertEquals("default_provider_name", record.get("attr_label_provider"));
		assertEquals("100.0", record.get("attr_label_latest_value"));
		assertEquals(6, record.size());
	}

	@Test
	public void testGetColumnNames() throws Exception {
		List<String> attributes = exporter.getColumnNames(makeFields("uk.gov.london", "populationDensity"));

		assertEquals("label", attributes.get(0));
		assertEquals("name", attributes.get(1));
		assertEquals("geometry", attributes.get(2));
		assertEquals("populationDensity_name", attributes.get(3));
		assertEquals("populationDensity_provider", attributes.get(4));
	}

	private List<Subject> makeSubjects() {
		return Collections.singletonList(SubjectUtils.getSubjectByLabel("E09000001"));
	}

	private List<Field> makeFields(String providerLabel, String attributeLabel) {
		return Collections.singletonList(
				new LatestValueField(attributeLabel,
						new AttributeMatcher(providerLabel, attributeLabel))
		);
	}

	private List<CSVRecord> getRecords(String csvString) throws IOException {
		CSVParser parser = CSVParser.parse(csvString, CSVFormat.DEFAULT.withHeader());
		return parser.getRecords();
	}
}