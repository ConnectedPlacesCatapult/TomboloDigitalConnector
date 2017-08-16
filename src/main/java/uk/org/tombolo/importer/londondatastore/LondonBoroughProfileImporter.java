package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.extraction.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for importing data from the official London Borough Profile.
 *
 * http://data.london.gov.uk/dataset/london-borough-profiles
 *
 * This importer only imports a few fields needed for the OrganiCity project.
 * It is not the most beautiful of implementations
 * but I am not willing to invest time in it until I know it is used more widely.
 * A more rigorous importing is desired but will be implemented on demand.
 *
 * Local: aHR0cHM6Ly9maWxlcy5kYXRhcHJlc3MuY29tL2xvbmRvbi9kYXRhc2V0L2xvbmRvbi1ib3JvdWdoLXByb2ZpbGVzLzIwMTUtMDktMjRUMTU6NDk6NTIvbG9uZG9uLWJvcm91Z2gtcHJvZmlsZXMuY3N2.csv
 */
public class LondonBoroughProfileImporter extends AbstractLondonDatastoreImporter implements Importer{
    private enum DatasourceId {londonBoroughProfiles};
    private enum AttributeId {populationDensity, householdIncome, medianHousePrice, fractionGreenspace, carbonEmission,
        carsPerHousehold};

    private static final String DATAFILE_SUFFIX = ".csv";
    private static final String DATAFILE
            = "https://files.datapress.com/london/dataset/london-borough-profiles/2015-09-24T15:49:52/london-borough-profiles.csv";

    public LondonBoroughProfileImporter(Config config) {
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case londonBoroughProfiles:
                Datasource datasource = new Datasource(
                        getClass(),
                        datasourceId.name(),
                        getProvider(),
                        "London Borough Profiles",
                        "Various London borough statistics");
                datasource.setUrl("http://data.london.gov.uk/dataset/london-borough-profiles");

                for (AttributeId attributeId : AttributeId.values()) {
                    datasource.addTimedValueAttribute(getAttribute(attributeId));
                }
                return datasource;
            default:
                throw new ConfigurationException("Unknown datasource " + datasourceIdString);
        }
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        CSVExtractor subjectLabelExtractor = new CSVExtractor(0);
        List<TimedValueExtractor> extractors = getExtractors(subjectLabelExtractor);

        String line = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                downloadUtils.fetchInputStream(new URL(DATAFILE), getProvider().getLabel(), DATAFILE_SUFFIX)));
        List<TimedValue> timedValueBuffer = new ArrayList<>();
        while ((line = br.readLine())!=null) {
            CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
            List<CSVRecord> records = parser.getRecords();
            subjectLabelExtractor.setCsvRecord(records.get(0));
            for(TimedValueExtractor extractor: extractors){
                ((CSVExtractor)extractor.getValueExtractor()).setCsvRecord(records.get(0));
                try {
                    timedValueBuffer.add(extractor.extract());
                }catch (UnknownSubjectLabelException e){
                    // No reason to panic even if Subject does not exist and no reason to run the rest of the extractors
                    // Keep Calm and Break
                    break;
                }
            }
        }
        br.close();
        saveAndClearTimedValueBuffer(timedValueBuffer);
    }

    private Attribute getAttribute(AttributeId attributeId){
        switch (attributeId){
            case populationDensity:
                return new Attribute(getProvider(),
                        AttributeId.populationDensity.name(), "Population Density", "Population density (per hectare) 2015",
                        Attribute.DataType.numeric
                );
            case householdIncome:
                return new Attribute(getProvider(),
                        AttributeId.householdIncome.name(),"Household Income","Modelled Household median income estimates 2012/13",
                        Attribute.DataType.numeric
                );
            case medianHousePrice:
                return new Attribute(getProvider(),
                        AttributeId.medianHousePrice.name(),"Median House Price","Median House Price, 2014",
                        Attribute.DataType.numeric
                );
            case fractionGreenspace:
                return new Attribute(getProvider(),
                        AttributeId.fractionGreenspace.name(),"Fraction Greenspace","% of area that is Greenspace, 2005",
                        Attribute.DataType.numeric
                );
            case carbonEmission:
                return new Attribute(getProvider(),
                        AttributeId.carbonEmission.name(),"Carbon Emission","Total carbon emissions (2013)",
                        Attribute.DataType.numeric
                );
            case carsPerHousehold:
                return new Attribute(getProvider(),
                        AttributeId.carsPerHousehold.name(),"Cars Per Household","Number of cars per household, (2011 Census)",
                        Attribute.DataType.numeric
                );
            default:
                return null;
        }
    }

    private List<TimedValueExtractor> getExtractors(SingleValueExtractor subjectLabelExtractor){
        List<TimedValueExtractor> extractors = new ArrayList<>();
        for (AttributeId attributeId : AttributeId.values()){
            extractors.add(getExtractor(attributeId, subjectLabelExtractor));
        }
        return extractors;
    }

    private TimedValueExtractor getExtractor(AttributeId attributeId, SingleValueExtractor subjectLabelExtractor){
        SubjectType subjectType = OaImporter.getSubjectType(OaImporter.OaType.localAuthority);
        switch (attributeId){
            case populationDensity:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectType,
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.populationDensity.name()),
                        new ConstantExtractor("2015-12-31T23:59:59"),
                        new CSVExtractor(6)
                        );
            case householdIncome:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectType,
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.householdIncome.name()),
                        new ConstantExtractor("2013-12-31T23:59:59"),
                        new CSVExtractor(41)
                );
            case medianHousePrice:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectType,
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.medianHousePrice.name()),
                        new ConstantExtractor("2014-12-31T23:59:59"),
                        new CSVExtractor(51)
                );
            case fractionGreenspace:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectType,
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.fractionGreenspace.name()),
                        new ConstantExtractor("2005-12-31T23:59:59"),
                        new CSVExtractor(58)
                );
            case carbonEmission:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectType,
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.carbonEmission.name()),
                        new ConstantExtractor("2013-12-31T23:59:59"),
                        new CSVExtractor(59)
                );
            case carsPerHousehold:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectType,
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.carsPerHousehold.name()),
                        new ConstantExtractor("2011-12-31T23:59:59"),
                        new CSVExtractor(62)
                );
            default:
                return null;
        }
    }
}
