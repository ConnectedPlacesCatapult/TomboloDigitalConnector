package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Importer;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class ONSClaimantsImporterTest extends AbstractTest {
    public Importer importer;

    @Before
    public void before() throws Exception {
        importer = new ONSClaimantsImporter();
        mockDownloadUtils(importer);
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(1,datasources.size());
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("lsoaClaimants");
        assertEquals(1, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {

        TestFactory.makeSubjectType("lsoa", "Lower Layer Super Output Areas");
        Subject london001A = TestFactory.makeSubject("lsoa","E01000001","City of London 001A",TestFactory.FAKE_POINT_GEOMETRY);
        Subject london001B = TestFactory.makeSubject("lsoa","E01000002","City of London 001B",TestFactory.FAKE_POINT_GEOMETRY);
        Subject london001C = TestFactory.makeSubject("lsoa","E01000003","City of London 001C",TestFactory.FAKE_POINT_GEOMETRY);

        Subject wyre010A = TestFactory.makeSubject("lsoa", "E01025542", "Wyre 011B", TestFactory.FAKE_POINT_GEOMETRY);
        Subject blaby010A = TestFactory.makeSubject("lsoa", "E01025613", "Blaby 010A", TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("lsoaClaimants");

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