package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.extraction.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
 */
public class LondonBoroughProfileImporter extends AbstractLondonDatastoreImporter {
    private enum DatasourceId {
        londonBoroughProfiles(new DatasourceSpec(
                LondonBoroughProfileImporter.class,
                "londonBoroughProfiles",
                "London Borough Profiles",
                "Various London borough statistics",
                "http://data.london.gov.uk/dataset/london-borough-profiles")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }
    private enum AttributeId {populationDensity, householdIncome, medianHousePrice, fractionGreenspace, carbonEmission,
        carsPerHousehold}

    private static final String DATAFILE_SUFFIX = ".csv";
    private static final String DATAFILE
            = "https://files.datapress.com/london/dataset/london-borough-profiles/2017-01-26T18:50:00/london-borough-profiles.csv";

    public LondonBoroughProfileImporter(Config config) {
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) throws Exception {
        List<Attribute> attributes = new ArrayList<>();
        Arrays.stream(AttributeId.values()).map(attributeId -> getAttribute(attributeId)).forEach(attributes::add);

        return attributes;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        CSVExtractor subjectLabelExtractor = new CSVExtractor(0);
        List<TimedValueExtractor> extractors = getExtractors(subjectLabelExtractor);

        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(
                downloadUtils.fetchFile(new URL(DATAFILE), getProvider().getLabel(), DATAFILE_SUFFIX)));
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

    private Attribute getAttribute(AttributeId attributeId) {
        switch (attributeId){
            case populationDensity:
                return new Attribute(getProvider(), AttributeId.populationDensity.name(),
                        "Population density (per hectare) 2015");
            case householdIncome:
                return new Attribute(getProvider(), AttributeId.householdIncome.name(),
                        "Modelled Household median income estimates 2012/13");
            case medianHousePrice:
                return new Attribute(getProvider(),AttributeId.medianHousePrice.name(),
                        "Median House Price, 2014");
            case fractionGreenspace:
                return new Attribute(getProvider(), AttributeId.fractionGreenspace.name(),
                        "% of area that is Greenspace, 2005");
            case carbonEmission:
                return new Attribute(getProvider(), AttributeId.carbonEmission.name(),
                        "Total carbon emissions (2013)");
            case carsPerHousehold:
                return new Attribute(getProvider(), AttributeId.carsPerHousehold.name(),
                        "Number of cars per household, (2011 Census)");
            default:
                throw new Error("Unknown attribute label: " + String.valueOf(attributeId.name()));
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
                        new ConstantExtractor("2016-12-31T23:59:59"),
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
                        new ConstantExtractor("2015-12-31T23:59:59"),
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
                        new ConstantExtractor("2014-12-31T23:59:59"),
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
                throw new Error("Unknown attribute label: " + String.valueOf(attributeId.name()));
        }
    }
}
