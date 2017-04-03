package uk.org.tombolo.importer.schools;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.Importer;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SchoolsImporterTest extends AbstractTest {
    private static Importer importer;

    @Before
    public void before(){
        importer = new SchoolsImporter();
        mockDownloadUtils(importer);
    }

    @Test
    public void testGetProvider(){
        Provider provider = importer.getProvider();
        assertEquals("uk.gov.education", provider.getLabel());
        assertEquals("Department of Education", provider.getName());
    }

    @Test
    public void testGetAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(1,datasources.size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("schoolsInEngland");
        assertEquals("https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/597965/EduBase_Schools_March_2017.xlsx",datasource.getRemoteDatafile());
        assertEquals("EduBase_Schools_March_2017.xlsx", datasource.getLocalDatafile());
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("schoolsInEngland");

        List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByLabel("schools"),"uk.gov.education_schools_100000.0");
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertEquals("Castleford Half Acres Primary School", subject.getName());

        String header = "URN\tLocal authority (code)\tLocal authority (name)\tEstablishment number\tEstablishment name\tStreet\tLocality\tAddress3\tTown\tCounty\tPostcode\tType of establishment\tStatutory highest age\tStatutory lowest age\tBoarders\tSixth form\tUKPRN\tPhase of education\tGender\tReligious character\tReligious ethos\tAdmissions policy\tWebsite address\tTelephone number\tHeadteacher\tEstablishment status\tReason establishment opened\tOpening date\tParliamentary Constituency (code)\tParliamentary Constituency (name)\tRegion\n";
        String value = "144332\t384\tWakefield\t2182\tCastleford Half Acres Primary School\tTemple Street\t\t\tCastleford\tWest Yorkshire\tWF10 5RE\tAcademy Converter\t11\t3\tNo Boarders\tDoes not have a sixth form\t10062845\tPrimary\tMixed\tDoes not apply\tDoes not apply\tNot applicable\twww.halfacres.com/\t01977723010\tMrs Mandy Pickles\tOpen\tAcademy Converter\t01/03/2017\tE14000836\tNormanton, Pontefract and Castleford\tYorkshire and the Humber";
        String[] headers = header.split("\\t,\\n");
        String[] values = value.split("\\t,\\n");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, headers[i], values[i]);
        }
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue school = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        System.out.println("Subject " + subject + ", school " + school + "attribute " + attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, school.getValue());
    }
}
