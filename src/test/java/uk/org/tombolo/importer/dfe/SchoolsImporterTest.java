package uk.org.tombolo.importer.dfe;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using files for test:
 *
 * Local: 3d89d3e5-85ed-3976-8d5c-6744bc044e8a.xlsx
 */
public class SchoolsImporterTest extends AbstractTest {
    private static SchoolsImporter importer;

    @Before
    public void before(){
        importer = new SchoolsImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void testGetProvider(){
        Provider provider = importer.getProvider();
        assertEquals("uk.gov.education", provider.getLabel());
        assertEquals("Department for Education", provider.getName());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(1,datasources.size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("schools");
        assertEquals("https://www.gov.uk/government/publications/schools-in-england/", datasource.getDatasourceSpec().getUrl());
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("schools", null, null, null);

        List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("uk.gov.education","dfeSchools"),"uk.gov.education_schools_100000.0");
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertEquals("Sir John Cass's Foundation Primary School", subject.getName());

        String header = "URN\tLocal authority (code)\tLocal authority (name)\tEstablishment number\tEstablishment name\tStreet\tLocality\tAddress3\tTown\tCounty\tPostcode\tType of establishment\tStatutory highest age\tStatutory lowest age\tBoarders\tSixth form\tUKPRN\tPhase of education\tGender\tReligious character\tReligious ethos\tAdmissions policy\tWebsite address\tTelephone number\tHeadteacher\tEstablishment status\tReason establishment opened\tOpening date\tParliamentary Constituency (code)\tParliamentary Constituency (name)\tRegion\n";
        String value = "100000\t201\tCity of London\t3614\tSir John Cass's Foundation Primary School\tSt James's Passage\tDuke's Place\t\tLondon\t\tEC3A 5DE\tVoluntary Aided School\t11\t3\tNo Boarders\tDoes not have a sixth form\t\tPrimary\tMixed\tChurch of England\tDoes not apply\tNot applicable\twww.sirjohncassprimary.org\t02072831147\tMr Tim Wilson\tOpen\tNot applicable\t\tE14000639\tCities of London and Westminster\tLondon\n";
        String[] headers = header.split("[\t\n]");
        String[] values = value.split("[\t\n]");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, AttributeUtils.nameToLabel(headers[i]), values[i]);
        }
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue school = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, school.getValue());
    }
}
