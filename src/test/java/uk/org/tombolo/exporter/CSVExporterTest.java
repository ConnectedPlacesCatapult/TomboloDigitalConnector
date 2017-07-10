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
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.value.LatestValueField;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CSVExporterTest extends AbstractTest {
	CSVExporter exporter = new CSVExporter();

	Subject testSubject;

	@Before
	public void addSubjectFixtures() {
		testSubject = TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testWrite() throws Exception {
		Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
		SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
		TestFactory.makeTimedValue(localAuthority,"E09000001", attribute, TestFactory.TIMESTAMP, 100d);

		Writer writer = new StringWriter();
		exporter.write(writer, makeSubjects(), makeFields("default_provider_label", "attr_label"));
		CSVRecord record = getRecords(writer.toString()).get(0);

		assertEquals("E09000001", record.get("label"));
		assertEquals("City of London", record.get("name"));
		assertNotNull(record.get("geometry"));
		assertEquals("100.0", record.get("attr_label"));
		assertEquals(4, record.size());
	}

	@Test
	public void testGetColumnNames() throws Exception {
		List<String> attributes = exporter.getColumnNames(makeFields("uk.gov.london", "populationDensity"));

		assertEquals("label", attributes.get(0));
		assertEquals("name", attributes.get(1));
		assertEquals("geometry", attributes.get(2));
	}

	private List<Subject> makeSubjects() {
		return Collections.singletonList(SubjectUtils.getSubjectByTypeAndLabel(testSubject.getSubjectType(), testSubject.getLabel()));
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