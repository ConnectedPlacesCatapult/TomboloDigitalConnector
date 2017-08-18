package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Remote: "http://www.nomisweb.co.uk/api/v01/dataset/NM_162_1.data.csv?" +
 *      "geography=1249902593...1249937345&" +
 *      "date=latest&" +
 *      "gender=0&" +
 *      "age=0&" +
 *      "measure=1&" +
 *      "measures=20100&" +
 *      "select=date_name,geography_name,geography_code,gender_name,age_name,measure_name,measures_name,obs_value,obs_status_name";
 * Local: ab9d3499b5faf5a8b6c3c49271ff2c19.csv
 */
public class ONSClaimantsImporterTest extends AbstractTest {
    public ONSClaimantsImporter importer;

    @Before
    public void before() throws Exception {
        importer = new ONSClaimantsImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("claimants"),datasources);
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("claimants");
        assertEquals(1, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {

        SubjectType lsoa = TestFactory.makeNamedSubjectType("lsoa");
        Subject london001A = TestFactory.makeSubject(lsoa,"E01000001","City of London 001A",TestFactory.FAKE_POINT_GEOMETRY);
        Subject london001B = TestFactory.makeSubject(lsoa,"E01000002","City of London 001B",TestFactory.FAKE_POINT_GEOMETRY);
        Subject london001C = TestFactory.makeSubject(lsoa,"E01000003","City of London 001C",TestFactory.FAKE_POINT_GEOMETRY);

        Subject wyre010A = TestFactory.makeSubject(lsoa, "E01025542", "Wyre 011B", TestFactory.FAKE_POINT_GEOMETRY);
        Subject blaby010A = TestFactory.makeSubject(lsoa, "E01025613", "Blaby 010A", TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("claimants", null, null, null);

        Attribute claimantsAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "claimantCount");

        //Jan-16,City of London 001A,E01000001,Total,All categories: Age 16+,Claimant count,Value,0,Normal Value
        TimedValue londonValueA = TimedValueUtils.getLatestBySubjectAndAttribute(london001A, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2016-01-31T23:59:59"),londonValueA.getId().getTimestamp());
        assertEquals(0d, londonValueA.getValue(), 0.1d);

        //Feb-16,City of London 001B,E01000002,Total,All categories: Age 16+,Claimant count,Value,0,Normal Value
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(london001B, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2016-02-29T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(0d, londonValueB.getValue(), 0.1d);

        //Feb-17,City of London 001C,E01000003,Total,All categories: Age 16+,Claimant count,Value,15,Normal Value
        TimedValue londonValueC = TimedValueUtils.getLatestBySubjectAndAttribute(london001C, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-02-28T23:59:59"),londonValueC.getId().getTimestamp());
        assertEquals(15d, londonValueC.getValue(), 0.1d);

        //Jun-17,Wyre 010A,E01025542,Total,All categories: Age 16+,Claimant count,Value,5,Normal Value
        TimedValue wyreValue = TimedValueUtils.getLatestBySubjectAndAttribute(wyre010A, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-06-30T23:59:59"),wyreValue.getId().getTimestamp());
        assertEquals(5d, wyreValue.getValue(), 0.1d);

        //Dec-17,Blaby 010A,E01025613,Total,All categories: Age 16+,Claimant count,Value,5,Normal Value
        TimedValue blabyValue = TimedValueUtils.getLatestBySubjectAndAttribute(blaby010A, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),blabyValue.getId().getTimestamp());
        assertEquals(5d, blabyValue.getValue(), 0.1d);
    }

}