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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using dbb3bc4c-7bd1-39ce-af7b-26dd9b2693fe.xls
 */
public class ONSBusinessDemographyImporterTest extends AbstractTest {
    public ONSBusinessDemographyImporter importer;
    Subject barking;
    Subject northEast;

    @Before
    public void before() throws Exception {
        importer = new ONSBusinessDemographyImporter();
        barking = TestFactory.makeNamedSubject("E09000002");
        northEast = TestFactory.makeNamedSubject("E12000001");
        mockDownloadUtils(importer);
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("ONSNewBusinessSurvival"),datasources);
    }

    @Test
    public void testGetDatasourcIds() throws Exception {
        Datasource datasourceCycle = importer.getDatasource("ONSNewBusinessSurvival");
        assertEquals(10, datasourceCycle.getTimedValueAttributes().size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasourceCycle = importer.getDatasource("ONSNewBusinessSurvival");
        assertEquals("https://www.ons.gov.uk/file?uri=/businessindustryandtrade/business/activitysizeandlocation/datasets/businessdemographyreferencetable/current/businessdemographyexceltables2016v2.xls", datasourceCycle.getDatasourceSpec().getUrl());
    }

    @Test
    public void testImporter() throws Exception {
        importer.importDatasource("ONSNewBusinessSurvival", null, null, null);
        Attribute survivalAttribute;
        Attribute percentAttribute;
        TimedValue barkingValue;
        TimedValue northValue;

        survivalAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_5_year_total");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, survivalAttribute);
        assertEquals(LocalDateTime.parse("2011-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(320d, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, survivalAttribute);
        assertEquals(LocalDateTime.parse("2011-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(3010d, northValue.getValue(), 0.1d);

        percentAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_5_year_fraction");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, percentAttribute);
        assertEquals(LocalDateTime.parse("2011-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(38.3233532934132d/100, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, percentAttribute);
        assertEquals(LocalDateTime.parse("2011-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(42.5742574257426d/100, northValue.getValue(), 0.1d);

        survivalAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_4_year_total");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, survivalAttribute);
        assertEquals(LocalDateTime.parse("2012-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(395d, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, survivalAttribute);
        assertEquals(LocalDateTime.parse("2012-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(3720d, northValue.getValue(), 0.1d);

        percentAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_4_year_fraction");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, percentAttribute);
        assertEquals(LocalDateTime.parse("2012-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(46.7455621301775d/100, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, percentAttribute);
        assertEquals(LocalDateTime.parse("2012-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(51.2044046799725d/100, northValue.getValue(), 0.1d);

        survivalAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_3_year_total");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, survivalAttribute);
        assertEquals(LocalDateTime.parse("2013-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(690d, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, survivalAttribute);
        assertEquals(LocalDateTime.parse("2013-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(5830d, northValue.getValue(), 0.1d);

        percentAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_3_year_fraction");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, percentAttribute);
        assertEquals(LocalDateTime.parse("2013-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(58.2278481012658d/100, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, percentAttribute);
        assertEquals(LocalDateTime.parse("2013-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(60.1961796592669d/100, northValue.getValue(), 0.1d);

        survivalAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_2_year_total");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, survivalAttribute);
        assertEquals(LocalDateTime.parse("2014-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(970d, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, survivalAttribute);
        assertEquals(LocalDateTime.parse("2014-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(7290d, northValue.getValue(), 0.1d);

        percentAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_2_year_fraction");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, percentAttribute);
        assertEquals(LocalDateTime.parse("2014-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(74.0458015267176d/100, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, percentAttribute);
        assertEquals(LocalDateTime.parse("2014-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(75.5440414507772d/100, northValue.getValue(), 0.1d);

        survivalAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_1_year_total");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, survivalAttribute);
        assertEquals(LocalDateTime.parse("2015-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(1580d, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, survivalAttribute);
        assertEquals(LocalDateTime.parse("2015-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(8865d, northValue.getValue(), 0.1d);

        percentAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "survival_1_year_fraction");

        barkingValue = TimedValueUtils.getLatestBySubjectAndAttribute(barking, percentAttribute);
        assertEquals(LocalDateTime.parse("2015-12-31T23:59:59"),barkingValue.getId().getTimestamp());
        assertEquals(94.8948948948949d/100, barkingValue.getValue(), 0.1d);

        northValue = TimedValueUtils.getLatestBySubjectAndAttribute(northEast, percentAttribute);
        assertEquals(LocalDateTime.parse("2015-12-31T23:59:59"),northValue.getId().getTimestamp());
        assertEquals(92.008303061754d/100, northValue.getValue(), 0.1d);
    }
}
