package uk.org.tombolo.importer.ons;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tbantis on 26/02/2018.
 */
public class ONSJobSeekersAllowanceImporter extends AbstractONSImporter {
    private static final Logger log = LoggerFactory.getLogger(ONSJobSeekersAllowanceImporter.class);

    private enum DatasourceId {
        ONSJobSeekersAllowance(new DatasourceSpec(
                ONSJobSeekersAllowanceImporter.class,
                "ONSJobSeekersAllowance",
                "Jobseeker's Allowance with rates and proportions",
                "Jobseeker's Allowance with rates and proportions, for both sexes. ",
                "https://www.nomisweb.co.uk/query/getFile.aspx?filename=2116022247.csv")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }
    public ONSJobSeekersAllowanceImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(
                OaImporter.OaType.localAuthority.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        String fileLocation = DatasourceId.ONSJobSeekersAllowance.datasourceSpec.getUrl();
        InputStreamReader isr = new InputStreamReader(
                downloadUtils.fetchInputStream(new URL(fileLocation), getProvider().getLabel(), ".csv"));

        // This dataset contains both subject types
        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(0));

        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        CSVParser csvFileParser = new CSVParser(isr, CSVFormat.DEFAULT);
        List<CSVRecord> csvRecords = csvFileParser.getRecords();
        Iterator<CSVRecord> rowIterator = csvRecords.iterator();

        // Skipping unrelevant rows
        int ignore = 0;
        while (ignore++ < 7) {
            rowIterator.next();
        }
        CSVRecord rowTime = rowIterator.next();
        int noOfColumns = rowTime.size();
        System.out.println("HERE: "+noOfColumns);
        // Looping through geographies
        for (int i=0; i <= 10; i = i+2) {
            String geograghy =  String.valueOf(rowTime.get(i)).trim();
            System.out.println(geograghy);
        }

//        // Looping through rows
//        while (rowIterator.hasNext()) {
//            Row row = rowIterator.next();
//
//
//        }
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "totalClaimants", "fractionOfResidentPopulation"};
        String[] description = { "Total claimants", "Proportion (as a fraction) of resident population aged 16-64 estimates"};

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], description[i]));

        }
        return attributes;
    }
}
