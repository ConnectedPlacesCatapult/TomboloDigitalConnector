package uk.org.tombolo.importer.ons;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.Importer;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for the ONS 2011 Census using the Nomisweb API.
 */
public class CensusImporter extends AbstractONSImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(CensusImporter.class);
    private static final LocalDateTime TIMESTAMP = TimedValueUtils.parseTimestampString("2011");

    // FIXME: Generalise this to any dataset from the census
    protected enum DatasourceId {
        qs701ew(new DataSourceID(
                "qs701ew",
                "Method of Travel to Work",
                "Method of travel to work. All usual residents aged 16 to 74.",
                "https://www.nomisweb.co.uk/census/2011/qs701ew",
                null)
        );

        private DataSourceID dataSourceID;

        DatasourceId(DataSourceID dataSourceID) {
            this.dataSourceID = dataSourceID;
        }
    };

    public CensusImporter(Config config) {
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }


    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        Datasource datasource = datasourceFromDatasourceId(datasourceId.dataSourceID);

        datasource.addAllTimedValueAttributes(getTimedValueAttributes(datasourceIdString));

        return datasource;
    }

    protected List<Attribute> getTimedValueAttributes(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        String headerRowUrl = getDataUrl(datasourceIdString)+"&recordlimit=0";
        File headerRowStream = downloadUtils.fetchFile(new URL(headerRowUrl), getProvider().getLabel(), ".csv");

        List<Attribute> attributes = new ArrayList<>();
        CSVParser csvParser = new CSVParser(new FileReader(headerRowStream), CSVFormat.RFC4180.withFirstRecordAsHeader());
        for (String header : csvParser.getHeaderMap().keySet()){
            if (header.startsWith(datasourceId.dataSourceID.getName() + ":")) {
                // The header starts with the same name as the label of the dataset
                String attributeLabel = attributeLabelFromHeader(header);
                attributes.add(new Attribute(getProvider(), attributeLabel, header, header, Attribute.DataType.numeric));
            }
        }
        return attributes;
    }

    private String attributeLabelFromHeader(String header){
        // FIXME: Make sure that this generalises over all datasets
        int start = header.indexOf(":");
        int end = header.indexOf(";");
        return header.substring(0, Math.min(63, end));
    }

    protected String getDataUrl(String datasourceIdString){
        // FIXME: Generalise this to any dataset from the census
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case qs701ew:
                return "https://www.nomisweb.co.uk/api/v01/dataset/"
                        +"nm_568_1"     // <- This should change according to dataset
                        +".bulk.csv?"
                        +"time=latest"
                        +"&"+"measures=20100"
                        +"&"+"rural_urban=total"
                        +"&"+"geography=TYPE298";
            default:
                return null;
        }
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        // Collect materialised attributes
        List<Attribute> attributes = new ArrayList<>();
        for(Attribute attribute : getTimedValueAttributes(datasource.getId())){
            attributes.add(AttributeUtils.getByProviderAndLabel(attribute.getProvider(),attribute.getLabel()));
        }

        // FIXME: Generalise this beyond LSOA
        SubjectType lsoa = OaImporter.getSubjectType(OaImporter.OaType.lsoa);
        List<TimedValue> timedValueBuffer = new ArrayList<>();
        String dataUrl = getDataUrl(datasource.getId());

        // FIXME: Use stream instead of file
        File dataStream = downloadUtils.fetchFile(new URL(dataUrl), getProvider().getLabel(), ".csv");

        CSVParser csvParser = new CSVParser(new FileReader(dataStream), CSVFormat.RFC4180.withFirstRecordAsHeader());
        int recordCount = 0;
        for (CSVRecord record : csvParser) {
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(lsoa, record.get("geography code"));
            if (subject != null) {
                for (Attribute attribute : attributes) {
                    String value = record.get(attribute.getName());
                    TimedValue timedValue = new TimedValue(subject, attribute, TIMESTAMP, Double.valueOf(value));
                    timedValueBuffer.add(timedValue);
                }
            }
            recordCount++;
        }

        saveAndClearTimedValueBuffer(timedValueBuffer);
    }
}
