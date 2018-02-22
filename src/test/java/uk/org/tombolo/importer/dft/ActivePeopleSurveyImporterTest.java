package uk.org.tombolo.importer.dft;

import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ConfigurationException;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * Using the following test data files:
 *
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536499/cw0105.ods
 * Local: 5a17f99d-fbdb-37f0-b669-cf2bc26cd192.ods
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536501/cw0104.ods
 * Local: 096ab00b-7aed-3ef1-99d6-7f906d0a770c.ods
 */
public class ActivePeopleSurveyImporterTest extends AbstractTest {
	Subject barking;
	Subject northEast;

	private static ActivePeopleSurveyImporter importer;

	@Before
	public void before(){
		importer = new ActivePeopleSurveyImporter();
		barking = TestFactory.makeNamedSubject("E09000002");
		northEast = TestFactory.makeNamedSubject("E12000001");

		mockDownloadUtils(importer);
	}

	@Test
	public void testGetProvider(){
		Provider provider = importer.getProvider();
		assertEquals("uk.gov.dft", provider.getLabel());
		assertEquals("Department for Transport", provider.getName());
	}

	@Test
	public void testGetDatasourcIds() throws Exception {
		Datasource datasourceCycle = importer.getDatasource("activePeopleCycle");
		assertEquals(20, datasourceCycle.getTimedValueAttributes().size());

		Datasource datasourceWalk = importer.getDatasource("activePeopleWalk");
		assertEquals(20, datasourceWalk.getTimedValueAttributes().size());
	}

	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasourceCycle = importer.getDatasource("activePeopleCycle");
		assertEquals("https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536501/cw0104.ods", datasourceCycle.getDatasourceSpec().getUrl());

		Datasource datasourceWalk = importer.getDatasource("activePeopleWalk");
		assertEquals("https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536499/cw0105.ods", datasourceWalk.getDatasourceSpec().getUrl());
	}

	@Test
	public void testImportDatasourceCycle() throws Exception {
		importer.importDatasource("activePeopleCycle", null, null, null);
		testTimedValue(barking, "fractionCycle_1pm", 8.78045207738247/100.);
		testTimedValue(barking, "fractionCycle_1pw", 5.32836466187781/100.);
		testTimedValue(barking, "fractionCycle_3pw", 3.47383911182684/100.);
		testTimedValue(barking, "fractionCycle_5pw", 2.25216470392245/100.);

		testTimedValue(barking, "fractionCycleRecreation_1pm", 5.60594483528578/100.);
		testTimedValue(barking, "fractionCycleRecreation_1pw", 2.80218046589897/100.);
		testTimedValue(barking, "fractionCycleRecreation_3pw", 1.34939644275285/100.);
		testTimedValue(barking, "fractionCycleRecreation_5pw", 0.496696955325056/100.);

		testTimedValue(barking, "fractionCycleUtility_1pm", 5.68354124684765/100.);
		testTimedValue(barking, "fractionCycleUtility_1pw", 3.98627584441459/100.);
		testTimedValue(barking, "fractionCycleUtility_3pw", 2.12649004125581/100.);
		testTimedValue(barking, "fractionCycleUtility_5pw", 1.09836901627015/100.);

		testTimedValue(barking, "fractionCycle_less30mins", 2.33784057148977/100.);
		testTimedValue(barking, "fractionCycle_less60mins", 2.50582080257662/100.);
		testTimedValue(barking, "fractionCycle_less120mins", 3.62258193746455/100.);
		testTimedValue(barking, "fractionCycle_more120mins", 0.0642569006754908/100.);

		testTimedValue(barking, "fractionCycleRecreation_less30mins", 0.833361436055727/100.);
		testTimedValue(barking, "fractionCycleRecreation_less60mins", 0.0);
		testTimedValue(barking, "fractionCycleRecreation_less120mins", 2.3501149566656/100.);
		testTimedValue(barking, "fractionCycleRecreation_more120mins", 1.24776087426578/100.);

		testTimedValue(northEast, "fractionCycle_1pm", 11.4734881143795/100.);
		testTimedValue(northEast, "fractionCycle_1pw", 7.22673716388823/100.);
		testTimedValue(northEast, "fractionCycle_3pw", 3.28735833967901/100.);
		testTimedValue(northEast, "fractionCycle_5pw", 1.461292873006865/100.);

		testTimedValue(northEast, "fractionCycleRecreation_1pm", 8.89353430531066/100.);
		testTimedValue(northEast, "fractionCycleRecreation_1pw", 4.86837218921677/100.);
		testTimedValue(northEast, "fractionCycleRecreation_3pw", 1.82438644705404/100.);
		testTimedValue(northEast, "fractionCycleRecreation_5pw", 0.574517832742658/100.);

		testTimedValue(northEast, "fractionCycleUtility_1pm", 3.82474956178167/100.);
		testTimedValue(northEast, "fractionCycleUtility_1pw", 2.7126103862472/100.);
		testTimedValue(northEast, "fractionCycleUtility_3pw", 1.31199440803075/100.);
		testTimedValue(northEast, "fractionCycleUtility_5pw", 0.545009700272197/100.);

		testTimedValue(northEast, "fractionCycle_less30mins", 1.898577484606/100.);
		testTimedValue(northEast, "fractionCycle_less60mins", 2.69974289860276/100.);
		testTimedValue(northEast, "fractionCycle_less120mins", 3.66087567426737/100.);
		testTimedValue(northEast, "fractionCycle_more120mins", 2.94009090255099/100.);

		testTimedValue(northEast, "fractionCycleRecreation_less30mins", 0.783861428720689/100.);
		testTimedValue(northEast, "fractionCycleRecreation_less60mins", 1.50124285530313/100.);
		testTimedValue(northEast, "fractionCycleRecreation_less120mins", 3.29680226978019/100.);
		testTimedValue(northEast, "fractionCycleRecreation_more120mins", 3.01798799180663/100.);
	}

	@Test
	public void testImportDatasourceWalk() throws Exception {
		importer.importDatasource("activePeopleWalk", null, null, null);
		testTimedValue(barking, "fractionWalk_1pm", 76.9030090772639/100.);
		testTimedValue(barking, "fractionWalk_1pw", 72.199281157446/100.);
		testTimedValue(barking, "fractionWalk_3pw", 58.7269634882311/100.);
		testTimedValue(barking, "fractionWalk_5pw", 50.2910035212338/100.);

		testTimedValue(barking, "fractionWalkRecreation_1pm", 33.4616568223557/100.);
		testTimedValue(barking, "fractionWalkRecreation_1pw", 23.8593142264925/100.);
		testTimedValue(barking, "fractionWalkRecreation_3pw", 15.6579693878178/100.);
		testTimedValue(barking, "fractionWalkRecreation_5pw", 10.3525541898709/100.);

		testTimedValue(barking, "fractionWalkUtility_1pm", 66.2510547476051/100.);
		testTimedValue(barking, "fractionWalkUtility_1pw", 62.1759514334227/100.);
		testTimedValue(barking, "fractionWalkUtility_3pw", 45.8789481337957/100.);
		testTimedValue(barking, "fractionWalkUtility_5pw", 31.4236733389727/100.);

		testTimedValue(barking, "fractionWalk_less30mins", 32.2978149068426/100.);
		testTimedValue(barking, "fractionWalk_less60mins", 20.0703324280372/100.);
		testTimedValue(barking, "fractionWalk_less120mins", 11.583800427608/100.);
		testTimedValue(barking, "fractionWalk_more120mins", 11.3146880855876/100.);

		testTimedValue(barking, "fractionWalkRecreation_less30mins", 7.28899719362023/100.);
		testTimedValue(barking, "fractionWalkRecreation_less60mins", 8.22558899020264/100.);
		testTimedValue(barking, "fractionWalkRecreation_less120mins", 7.08545975691358/100.);
		testTimedValue(barking, "fractionWalkRecreation_more120mins", 6.18821719981224/100.);

		testTimedValue(northEast, "fractionWalk_1pm", 84.6603627867862/100.);
		testTimedValue(northEast, "fractionWalk_1pw", 80.420371835631/100.);
		testTimedValue(northEast, "fractionWalk_3pw", 61.9814804771301/100.);
		testTimedValue(northEast, "fractionWalk_5pw", 51.1685506629201/100.);

		testTimedValue(northEast, "fractionWalkRecreation_1pm", 52.3777630019931/100.);
		testTimedValue(northEast, "fractionWalkRecreation_1pw", 43.1440866176247/100.);
		testTimedValue(northEast, "fractionWalkRecreation_3pw", 26.8598126874634/100.);
		testTimedValue(northEast, "fractionWalkRecreation_5pw", 18.756486407805/100.);

		testTimedValue(northEast, "fractionWalkUtility_1pm", 57.5327049215511/100.);
		testTimedValue(northEast, "fractionWalkUtility_1pw", 51.2459539736107/100.);
		testTimedValue(northEast, "fractionWalkUtility_3pw", 34.422806690624/100.);
		testTimedValue(northEast, "fractionWalkUtility_5pw", 23.9537859400873/100.);

		testTimedValue(northEast, "fractionWalk_less30mins", 28.1561140534647/100.);
		testTimedValue(northEast, "fractionWalk_less60mins", 28.6345656925724/100.);
		testTimedValue(northEast, "fractionWalk_less120mins", 17.266963589042/100.);
		testTimedValue(northEast, "fractionWalk_more120mins", 9.57845217354126/100.);

		testTimedValue(northEast, "fractionWalkRecreation_less30mins", 9.43992759955879/100.);
		testTimedValue(northEast, "fractionWalkRecreation_less60mins", 16.6431823463373/100.);
		testTimedValue(northEast, "fractionWalkRecreation_less120mins", 14.8732010356012/100.);
		testTimedValue(northEast, "fractionWalkRecreation_more120mins", 8.67072200783574/100.);
	}
	private void testTimedValue(Subject subject, String attributeLabel, Double value) {
		Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
		TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
		assertEquals(value, val.getValue(),0.00001);
	}

}
