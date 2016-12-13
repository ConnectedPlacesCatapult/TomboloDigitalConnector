package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.extraction.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
 */
public class LondonBoroughProfileImporter extends AbstractLondonDatastoreImporter implements Importer{
    private enum DatasourceId {londonBoroughProfiles};
    private enum AttributeId {populationDensity, householdIncome, medianHousePrice, fractionGreenspace, carbonEmission,
        carsPerHousehold};

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return datasourcesFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId;
        try {
            datasourceId = DatasourceId.valueOf(datasourceIdString);
        }catch(IllegalArgumentException e){
            throw new ConfigurationException("Unknown datasource " + datasourceIdString);
        }
        switch (datasourceId){
            case londonBoroughProfiles:
                Datasource datasource = new Datasource(
                        datasourceId.name(),
                        getProvider(),
                        "London Borough Profiles",
                        "Various London borough statistics");
                datasource.setUrl("http://data.london.gov.uk/dataset/london-borough-profiles");
                datasource.setRemoteDatafile("https://files.datapress.com/london/dataset/london-borough-profiles/2015-09-24T15:49:52/london-borough-profiles.csv");
                datasource.setLocalDatafile("LondonDatastore/london-borough-profiles.csv");

                for (AttributeId attributeId : AttributeId.values()) {
                    datasource.addTimedValueAttribute(getAttribute(attributeId));
                }
                return datasource;
            default:
                throw new ConfigurationException("Unknown datasource " + datasourceIdString);
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        saveDatasourceMetadata(datasource);

        CSVExtractor subjectLabelExtractor = new CSVExtractor(0);
        List<TimedValueExtractor> extractors = getExtractors(subjectLabelExtractor);

        File localFile = downloadUtils.getDatasourceFile(datasource);
        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(localFile));
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
        TimedValueUtils.save(timedValueBuffer);
        return timedValueBuffer.size();
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
        switch (attributeId){
            case populationDensity:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.populationDensity.name()),
                        new ConstantExtractor("2015-12-31T23:59:59"),
                        new CSVExtractor(6)
                        );
            case householdIncome:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.householdIncome.name()),
                        new ConstantExtractor("2013-12-31T23:59:59"),
                        new CSVExtractor(41)
                );
            case medianHousePrice:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.medianHousePrice.name()),
                        new ConstantExtractor("2014-12-31T23:59:59"),
                        new CSVExtractor(51)
                );
            case fractionGreenspace:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.fractionGreenspace.name()),
                        new ConstantExtractor("2005-12-31T23:59:59"),
                        new CSVExtractor(58)
                );
            case carbonEmission:
                return new TimedValueExtractor(
                        getProvider(),
                        subjectLabelExtractor,
                        new ConstantExtractor(AttributeId.carbonEmission.name()),
                        new ConstantExtractor("2013-12-31T23:59:59"),
                        new CSVExtractor(59)
                );
            case carsPerHousehold:
                return new TimedValueExtractor(
                        getProvider(),
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
