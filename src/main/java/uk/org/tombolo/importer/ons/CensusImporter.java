package uk.org.tombolo.importer.ons;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.utils.JSONReader;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Importer for the ONS 2011 Census using the Nomisweb API.
 */

/**
 * INFO FOR RECIPE
 *
 * "importerClass": "uk.org.tombolo.importer.ons.OaImporter"
 * "datasourceId": "lsoa", "msoa", "localAuthority"
 * "provider": "uk.gov.ons"
 * "subjectTypes": ["lsoa", "msoa", "localAuthority"]
 *
 * "timedValueAttributes": [ check catalogue.json ]
 *
 * "fixedValueAttributes": []
 */
public class CensusImporter extends AbstractONSImporter {
    private static Logger log = LoggerFactory.getLogger(CensusImporter.class);
    private static final String SEED_URL = "https://www.nomisweb.co.uk/api/v01/dataset/def.sdmx.json";
    private ArrayList<CensusDescription> descriptions = new ArrayList<>();
    private static final Set<String> BLACK_LIST_HEADERS
            = new HashSet<>(Arrays.asList("date", "geography", "geography code", "Rural Urban"));

    /**
     * These are the geography codes mapped by nomis for respective granularity
     * e.g https://www.nomisweb.co.uk/api/v01/dataset/NM_522_1.bulk.csv?time=latest&measures=20100&rural_urban=total&geography=TYPE298
     * Providing geography=TYPE298 would download data for lsoa
     */
    private static final Map<String, String> GEOGRAPHIES = new HashMap<String, String>() {{
               put("lsoa", "TYPE298");
               put("msoa", "TYPE297");
               put("localAuthority", "TYPE463");
    }};

    @Override
    public List<String> getDatasourceIds() {
        try {
            return getSeedData().stream().map(CensusDescription::getDataSetTable)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            log.error("An error has occurred while downloading DatasourceID's" + e.getMessage());
        }

        return Collections.emptyList();
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return subjectRecipes.stream().filter(subjectRecipe ->
            EnumUtils.isValidEnum(OaImporter.OaType.class, subjectRecipe.getSubjectType())).map(subjectRecipe ->
                subjectRecipe.getSubjectType()).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        for (CensusDescription description : descriptions) {
            if (description.getDataSetTable().equalsIgnoreCase(datasourceIdString)) {
                return new DatasourceSpec(getClass(), datasourceIdString, description.getDataSetDescription(), description.getDataSetDescription(),
                        "https://www.nomisweb.co.uk/census/2011/" + datasourceIdString);
            }
        }
        throw new Error("Unknown data-source-id: " + datasourceIdString);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceIdString) throws Exception {
        List<Attribute> attributes = new ArrayList<>();
        for (SubjectRecipe subjectRecipe : subjectRecipes) {
            String headerRowUrl = getDataUrl(datasourceIdString, subjectRecipe.getSubjectType()) + "&recordlimit=0";
            File headerRowStream = downloadUtils.fetchFile(new URL(headerRowUrl), getProvider().getLabel(), ".csv");
            CSVParser csvParser = new CSVParser(new FileReader(headerRowStream), CSVFormat.RFC4180.withFirstRecordAsHeader());

            for (String header : csvParser.getHeaderMap().keySet()) {
                if (!BLACK_LIST_HEADERS.contains(header)) {
                    String attributeLabel = attributeLabelFromHeader(header);
                    attributes.add(new Attribute(getProvider(), attributeLabel, header));
                    System.out.print(attributeLabel);
                }
            }
        }
        return attributes;
    }

    private String attributeLabelFromHeader(String header) {
        int end = header.indexOf(";");
        return header.substring(0, end);
    }

    protected String getDataUrl(String datasourceIdString, String geography) {
        return "https://www.nomisweb.co.uk/api/v01/dataset/"
                + getRecordId(datasourceIdString)
                + ".bulk.csv?"
                + "time=latest"
                + "&" + "measures=20100"
                + "&" + "rural_urban=total"
                + "&" + "geography=" + GEOGRAPHIES.get(geography);
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        LocalDateTime TIMESTAMP = TimedValueUtils.parseTimestampString("2011");

        // Collect materialised attributes
        List<Attribute> attributes = new ArrayList<>();
        for (Attribute attribute : datasource.getTimedValueAttributes()) {
            attributes.add(AttributeUtils.getByProviderAndLabel(attribute.getProvider(), attribute.getLabel()));
        }

        // Looping through all the subjects provided in the recipe and saving there respective values
        for (SubjectRecipe subjectTypeFromRecipe : subjectRecipes) {
            OaImporter.OaType oaType = OaImporter.OaType.valueOf(subjectTypeFromRecipe.getSubjectType());
            SubjectType subjectType = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                    oaType.name(), oaType.datasourceSpec.getDescription());

            List<TimedValue> timedValueBuffer = new ArrayList<>();
            String dataUrl = getDataUrl(datasource.getDatasourceSpec().getId(), subjectTypeFromRecipe.getSubjectType());

            InputStream dataStream = downloadUtils.fetchInputStream(
                    new URL(dataUrl), getProvider().getLabel(), ".csv");

            CSVParser csvParser = new CSVParser(new InputStreamReader(dataStream),
                    CSVFormat.RFC4180.withFirstRecordAsHeader());

            csvParser.forEach(record -> {
                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, record.get("geography code"));
                if (subject != null) {
                    attributes.forEach(attribute -> {
                        String value = record.get(attribute.getDescription());
                        TimedValue timedValue = new TimedValue(subject, attribute, TIMESTAMP, Double.valueOf(value));
                        timedValueBuffer.add(timedValue);
                    });
                }
            });

            saveAndClearTimedValueBuffer(timedValueBuffer);
        }
    }

    private ArrayList<CensusDescription> getSeedData() throws IOException {

        ArrayList<LinkedHashMap<String, List<String>>> jsonData =
                new JSONReader(downloadUtils.fetchInputStream(new URL(SEED_URL), "uk.gov.ons", ".json"),
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

    private String getRecordId(String dataSourceId) {
        for (CensusDescription description : descriptions) {
            if (description.getDataSetTable().equalsIgnoreCase(dataSourceId)) {
                return description.getDataSetID();
            }
        }
        throw new Error("Unknown data-source-id: " + dataSourceId);
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
}