package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.CSVUtils;
import uk.org.tombolo.importer.utils.extraction.CSVExtractor;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Nomisweb Claimants count importer
 *
 * https://www.nomisweb.co.uk/query/select/getdatasetbytheme.asp?theme=72
 *
 */
public class ONSClaimantsImporter extends AbstractONSImporter implements Importer {

    private enum DatasourceId {claimants};

    private Datasource[] datasources = {
            new Datasource(
                    getClass(),
                    DatasourceId.claimants.name(),
                    getProvider(),
                    "Claimants per LSOA",
                    "This experimental series counts the number of people claiming Jobseeker's Allowance plus those " +
                            "who claim Universal Credit and are required to seek work and be available for work and " +
                            "replaces the number of people claiming Jobseeker's Allowance as the headline indicator " +
                            "of the number of people claiming benefits principally for the reason of being unemployed. " +
                            "The JSA datasets have all been moved to a new Jobseeker's Allowance theme.")
    };

    private static final String DATAFILE = "http://www.nomisweb.co.uk/api/v01/dataset/NM_162_1.data.csv?" +
            "geography=1249902593...1249937345&" +
            "date=latest&" +
            "gender=0&" +
            "age=0&" +
            "measure=1&" +
            "measures=20100&" +
            "select=date_name,geography_name,geography_code,gender_name,age_name,measure_name,measures_name,obs_value,obs_status_name";

    private enum AttributeId {claimantCount};

    public ONSClaimantsImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case claimants:
                Datasource datasource = datasources[datasourceId.ordinal()];
                datasource.setUrl("https://www.nomisweb.co.uk/query/select/getdatasetbytheme.asp?theme=72");

                datasource.addAllTimedValueAttributes(getAttributes());
                return datasource;
            default:
                throw new Error("Unknown datasource");
        }
    }
    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        SubjectType subjectType = OaImporter.getSubjectType(OaImporter.OaType.lsoa);

        CSVExtractor subjectLabelExtractor = new CSVExtractor(2);
        CSVExtractor timestampExtractor = new CSVExtractor(0);
        CSVExtractor valueExtractor = new CSVExtractor(7);
        List<TimedValueExtractor> extractors = new ArrayList<>();
        extractors.add(new TimedValueExtractor(
                getProvider(),
                subjectType,
                subjectLabelExtractor,
                new ConstantExtractor(AttributeId.claimantCount.name()),
                timestampExtractor,
                valueExtractor
                )
        );

        File localFile = downloadUtils.fetchFile(new URL(DATAFILE), getProvider().getLabel(), ".csv");
        CSVUtils.extractAndSaveTimedValues(extractors, this, localFile);
    }

    private List<Attribute> getAttributes(){
        List<Attribute> attributes = new ArrayList<>();

        attributes.add(new Attribute(getProvider(), AttributeId.claimantCount.name(), "Claimant Count", "Number of claimants", Attribute.DataType.numeric));

        return attributes;
    }
}
