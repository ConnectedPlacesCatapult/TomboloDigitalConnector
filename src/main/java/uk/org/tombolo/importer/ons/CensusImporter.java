package uk.org.tombolo.importer.ons;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.JSONReader;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Importer for the ONS 2011 Census using the Nomisweb API.
 */
public class CensusImporter extends AbstractONSImporter implements Importer {
    private static Logger log = LoggerFactory.getLogger(CensusImporter.class);
    private static final LocalDateTime TIMESTAMP = TimedValueUtils.parseTimestampString("2011");
    private static final String SEED_URL = "https://www.nomisweb.co.uk/api/v01/dataset/def.sdmx.json";
    private String RECORD_ID = "";
    private ArrayList<CensusDescription> descriptions = new ArrayList<>();

    public CensusImporter(Config config) throws IOException {
        super(config);
        datasourceIds = getDataSourceIDs();
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {

        Datasource datasource = datasourceFromDatasourceId(getDataSourceObject(datasourceIdString));

        datasource.addAllTimedValueAttributes(getTimedValueAttributes(datasourceIdString));

        return datasource;
    }

    protected List<Attribute> getTimedValueAttributes(String datasourceIdString) throws Exception {
        String headerRowUrl = getDataUrl(datasourceIdString)+"&recordlimit=0";
        File headerRowStream = downloadUtils.fetchFile(new URL(headerRowUrl), getProvider().getLabel(), ".csv");

        List<Attribute> attributes = new ArrayList<>();
        CSVParser csvParser = new CSVParser(new FileReader(headerRowStream), CSVFormat.RFC4180.withFirstRecordAsHeader());
        // The header starts with the same name as the label of the dataset
        csvParser.getHeaderMap().keySet().stream().filter(header -> header.contains(":"))
                .filter(header -> getDataSourceObject(datasourceIdString).getName()
                .startsWith(header.toLowerCase().substring(0, header.indexOf(":")))).forEach(header -> {
                 String attributeLabel = attributeLabelFromHeader(header);
            attributes.add(new Attribute(getProvider(), attributeLabel, header, header, Attribute.DataType.numeric));
        });
        return attributes;
    }

    private String attributeLabelFromHeader(String header){
        // FIXME: Make sure that this generalises over all datasets
        int start = header.indexOf(":");
        int end = header.indexOf(";");
        return header.substring(0, Math.min(63, end));
    }

    protected String getDataUrl(String datasourceIdString) {

        if ("".equals(RECORD_ID)) getDataSourceObject(datasourceIdString);

        return "https://www.nomisweb.co.uk/api/v01/dataset/"
                + RECORD_ID
                +".bulk.csv?"
                +"time=latest"
                +"&"+"measures=20100"
                +"&"+"rural_urban=total"
                +"&"+"geography=TYPE298";
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

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
        InputStream dataStream = downloadUtils.fetchInputStream(
                                                new URL(dataUrl), getProvider().getLabel(), ".csv");

        CSVParser csvParser = new CSVParser(new InputStreamReader(dataStream),
                                                                CSVFormat.RFC4180.withFirstRecordAsHeader());

        csvParser.forEach(record -> {
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(lsoa, record.get("geography code"));
            if (subject != null) {
                attributes.forEach(attribute -> {
                    String value = record.get(attribute.getName());
                    TimedValue timedValue = new TimedValue(subject, attribute, TIMESTAMP, Double.valueOf(value));
                    timedValueBuffer.add(timedValue);
                });
            }
        });

        saveAndClearTimedValueBuffer(timedValueBuffer);
    }

    private ArrayList<CensusDescription> getSeedData() throws IOException {

        ArrayList<LinkedHashMap<String, List<String>>> jsonData =
                new JSONReader(new DownloadUtils("/tmp")
                        .fetchJSONStream(new URL(SEED_URL), "uk.gov.ons"),
                        Arrays.asList("id", "value")).getData();

        String regEx = "(qs)(\\d+)(ew)";
        Pattern pattern = Pattern.compile(regEx);

        jsonData.forEach(value -> {
            String prev = "";
            for (List<String> v : value.values()) {
                for (String s : v) {
                    if (s.toLowerCase().startsWith("nm_")) prev = s.toLowerCase();

                    Matcher matcher = pattern.matcher(s.toLowerCase());
                    if (matcher.find()) {
                        CensusDescription description = new CensusDescription();
                        description.setDataSetID(prev);
                        description.setDataSetTable(matcher.group());
                        description.setDataSetDescription(s.toLowerCase().substring(matcher.end() + 3).trim());
                        descriptions.add(description);
                    }
                }

            }
        });

        return descriptions;
    }

    private ArrayList<String> getDataSourceIDs () throws IOException {
        return getSeedData().stream().map(CensusDescription::getDataSetTable)
                                             .collect(Collectors.toCollection(ArrayList::new));
    }

    private DataSourceID getDataSourceObject (String dataSourceId) {

        for (CensusDescription description : descriptions) {
            if (description.getDataSetTable().equalsIgnoreCase(dataSourceId)) {
                RECORD_ID = description.getDataSetID();
                return new DataSourceID(dataSourceId, description.getDataSetDescription(), description.getDataSetDescription(),
                    "https://www.nomisweb.co.uk/census/2011/" + dataSourceId, null);
            }
        }

        return null;
    }
}



class CensusDescription {
    private String dataSetID;
    private String dataSetTable;
    private String dataSetDescription;

    String getDataSetID() {
        return dataSetID;
    }

    String getDataSetTable() {
        return dataSetTable;
    }

    String getDataSetDescription() {
        return dataSetDescription;
    }

    void setDataSetID(String dataSetID) {
        this.dataSetID = dataSetID;
    }

    void setDataSetTable(String dataSetTable) {
        this.dataSetTable = dataSetTable;
    }

    void setDataSetDescription(String dataSetDescription) {
        this.dataSetDescription = dataSetDescription;
    }
}


