package uk.org.tombolo.importer.spacesyntax;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.ZipUtils;
import uk.org.tombolo.importer.utils.GeotoolsDataStoreUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by tbantis on 08/03/2018.
 */
public class OpenMappingImporter extends AbstractImporter{
    private static Logger log = LoggerFactory.getLogger(OpenMappingImporter.class);
    private static final int LABEL_COLUMN_INDEX = 0;

    private enum DatasourceId {
        SpaceSyntaxOpenMapping(new DatasourceSpec(
                OpenMappingImporter.class,
                "SpaceSyntaxOpenMapping",
                "Space Syntax Open Mapping importer",
                "Space Syntax OpenMapping is a Great Britain wide dataset that includes analytic measures of "+
                        "the road network. All entities in the dataset have been coordinated with commonly used data collection"+
                        " boundaries, so that spatial characteristics can easily be compared to data at LSOA or Local Authority level.\n" +
                        " The data is licensed under CC BY-SA 4.0. This allows people to use and modify the data as long as"+
                        " it is attributed to Space Syntax, and that any modifications are shared back under the same terms."+
                        " By licensing the data in this way we hope to encourage wider exploration, innovation and application.",
                "https://github.com/FutureCitiesCatapult/TomboloOpenData/raw/master/ssx_openmapping_gb_v1_greaterlondon.zip")
        )
        ;

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public OpenMappingImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    public static final Provider PROVIDER = new Provider(
            "com.spacesyntax",
            "Space Syntax"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        SubjectType subjectType = SubjectTypeUtils.getOrCreate(
                getProvider(),
                "space_syntax",
                "space syntax"
        );

        URL url  = new URL(getDatasourceSpec(datasource.getDatasourceSpec().getId()).getUrl());
        File localFile = downloadUtils.fetchFile(url, getProvider().getLabel(), ".zip");
        Path dir = ZipUtils.unzipToTemporaryDirectory(localFile);

        File file = new File(dir + "/ssx_openmapping_gb_v1_greaterlondon.csv");
        InputStream isr = new FileInputStream(file);

        BufferedReader br = new BufferedReader(new FileReader(file));

        List<TimedValue> timedValues = new ArrayList<>();
        List<FixedValue> fixedValues = new ArrayList<>();
        LocalDateTime timestamp = TimedValueUtils.parseTimestampString("2018");
        List<Subject> subjects = new ArrayList<>();
        String label;
        String name;

        int batchSize = 10000;
        String line = null;
        long batchNumber = 1;

        List<String> mylist = null;
        while ((line = br.readLine()) != null) {
            log.info("Preparing to write #"+ batchNumber + " batch");
            mylist = readBatch(br, batchSize); // get/catch your (List) result here as returned from readBatch() method

            for (int i = 0; i < mylist.size(); i++) {
                String oneLine = mylist.get(i);
                String otherThanQuote = " [^\"] ";
                String quotedString = String.format(" \" %s* \" ", otherThanQuote);

                String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                                ",                         "+ // match a comma
                                "(?=                       "+ // start positive look ahead
                                "  (?:                     "+ //   start non-capturing group 1
                                "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                                "    %s                    "+ //     match 'quotedString'
                                "  )*                      "+ //   end group 1 and repeat it zero or more times
                                "  %s*                     "+ //   match 'otherThanQuote'
                                "  $                       "+ // match the end of the string
                                ")                         ", // stop positive look ahead
                        otherThanQuote, quotedString, otherThanQuote);

                List<String> records = new ArrayList<>();

                String[] tokens = oneLine.split(regex, -1);
                for(String t : tokens) {
                    records.add(t.toString());
                }

                String geography = records.get(35).trim().replace("\"","");
                label = getProvider().getLabel()+"_"+records.get(LABEL_COLUMN_INDEX);
                name = records.get(LABEL_COLUMN_INDEX);

                Subject subject = new Subject(
                        subjectType,
                        label,
                        name,
                        getShape(geography)
                );

                if (subject == null) {
                    log.warn("Geometry not found for " + geography + ": Skipping...");
                    continue;
                }

                int attributeIndex = 0;
                for (Attribute attribute : datasource.getTimedValueAttributes()) {
                    try {
                        Double record = Double.parseDouble(records.get(attributeIndex).trim());
                        timedValues.add(new TimedValue(subject,
                                attribute,
                                timestamp,
                                record));
                    } catch (NumberFormatException e){
                        String record = records.get(attributeIndex);
                        fixedValues.add(new FixedValue(subject,attribute,record));
                    }
                    attributeIndex++;
                }
                subjects.add(subject);
            }
            saveAndClearSubjectBuffer(subjects);
            saveAndClearTimedValueBuffer(timedValues);
            saveAndClearFixedValueBuffer(fixedValues);
            batchNumber++;
        }
        br.close();
        br = null;
    }
    private static List<String> readBatch(BufferedReader br, int batchSize) throws IOException {
        List<String> result = new ArrayList<>();
        for (int i = 1; i < batchSize; i++) {
            String line = br.readLine();
            if (line != null) {
                result.add(line);
            } else {
                return result;
            }
        }
        return result;
    }
    public Geometry getShape(String wtk) throws FactoryException, TransformException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
        WKTReader reader = new WKTReader(geometryFactory);
        MathTransform crsTransform = GeotoolsDataStoreUtils.makeCrsTransform("EPSG:27700");

        try {
            LineString line = (LineString) reader.read(wtk);
            Geometry transformedGeom = JTS.transform(line, crsTransform);
            return transformedGeom;
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("Not a valid geometry");
            return null;
        }
    }

    private enum AttributeId {id,metres,meridian_id,meridian_gid,meridian_code,
        meridian_osodr,meridian_number,meridian_road_name,meridian_indicator,
        meridian_class,choice2km,choice2kmrank,choice2kmlog,nodecount2km,integration2km,
        integration2kmrank,choice10km,choice10kmrank,choice10kmlog,nodecount10km,integration10km,
        integration10kmrank,choice100km,choice100kmlog,nodecount100km,integration100km,oa11cd,wz11cd,
        lsoa11cd,lsoa11nm,msoa11cd,msoa11nm,lad11cd,lad11nm,meridian_class_scale,wkt};

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        return Arrays.asList(
                new Attribute(getProvider(), AttributeId.id.name(), "Unique integer id of a road segment"),
                new Attribute(getProvider(), AttributeId.metres.name(), "Length of road segment in metres"),
                new Attribute(getProvider(), AttributeId.meridian_id.name(), "OS Meridian id. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_gid.name(), "OS Meridian g_id derived columns. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_code.name(), "OS Meridian meridian_code. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_osodr.name(), "OS Meridian meridian_osodr. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_number.name(), "OS Meridian meridian_number. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_road_name.name(), "OS Meridian meridian_road_name. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_indicator.name(), "OS Meridian meridian_indicator. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.meridian_class.name(), "OS Meridian meridian_class. Refer to the OS specification (https://www.ordnancesurvey.co.uk/docs/user-guides/meridian-2-user-guide.pdf) for more details."),
                new Attribute(getProvider(), AttributeId.choice2km.name(), "Choice 2km. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.choice2kmrank.name(), "Choice 2km rank. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.choice2kmlog.name(), "Choice 2km base 10 log. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.nodecount2km.name(), "nodecount2km"),
                new Attribute(getProvider(), AttributeId.integration2km.name(), "Integration 2km. Integration is based on the graph measure of Closeness Centrality. Space syntax adapts this to measure the number of street segments within a defined distance, accounting for their distance and change in direction from the starting segment. We have included Integration as it describes patterns of centrality"),
                new Attribute(getProvider(), AttributeId.integration2kmrank.name(), "Integration 2km rank. Integration is based on the graph measure of Closeness Centrality. Space syntax adapts this to measure the number of street segments within a defined distance, accounting for their distance and change in direction from the starting segment. We have included Integration as it describes patterns of centrality"),
                new Attribute(getProvider(), AttributeId.choice10km.name(), "Choice 10km. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.choice10kmrank.name(), "Choice 10km rank. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.choice10kmlog.name(), "Choice 10km base 10 log. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.nodecount10km.name(), "nodecount10km"),
                new Attribute(getProvider(), AttributeId.integration10km.name(), "Integration 10km. Integration is based on the graph measure of Closeness Centrality. Space syntax adapts this to measure the number of street segments within a defined distance, accounting for their distance and change in direction from the starting segment. We have included Integration as it describes patterns of centrality"),
                new Attribute(getProvider(), AttributeId.integration10kmrank.name(), "Integration 10km rank. Integration is based on the graph measure of Closeness Centrality. Space syntax adapts this to measure the number of street segments within a defined distance, accounting for their distance and change in direction from the starting segment. We have included Integration as it describes patterns of centrality"),
                new Attribute(getProvider(), AttributeId.choice100km.name(), "Choice 100km. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.choice100kmlog.name(), "Choice 100km base 10 log. Choice is based on the graph measure of Betweenness Centrality. Space syntax adapts this to measure how often a street segment appears on the simplest route between all other street segments in the network. The simplest route is defined as the route that undergoes the least total change in direction from start point to end point when linking all segments within a defined distance - e.g. 2,000m, 10,000m. We have included choice in the dataset as it often describes the distribution of movement. At 2,000m this will identify where pedestrian movement is likely to be higher, at 10km it will identify where vehicular movement is likely to be higher."),
                new Attribute(getProvider(), AttributeId.nodecount100km.name(), "nodecount100km"),
                new Attribute(getProvider(), AttributeId.integration100km.name(), "Integration 100km. Integration is based on the graph measure of Closeness Centrality. Space syntax adapts this to measure the number of street segments within a defined distance, accounting for their distance and change in direction from the starting segment. We have included Integration as it describes patterns of centrality"),
                new Attribute(getProvider(), AttributeId.oa11cd.name(), "Census output area"),
                new Attribute(getProvider(), AttributeId.wz11cd.name(), "Census workplace zones"),
                new Attribute(getProvider(), AttributeId.lsoa11cd.name(), "Census LSOA code"),
                new Attribute(getProvider(), AttributeId.lsoa11nm.name(), "Census LSOA name"),
                new Attribute(getProvider(), AttributeId.msoa11cd.name(), "Census MSOA code"),
                new Attribute(getProvider(), AttributeId.msoa11nm.name(), "Census MSOA name"),
                new Attribute(getProvider(), AttributeId.lad11cd.name(), "Census LA code"),
                new Attribute(getProvider(), AttributeId.lad11nm.name(), "Census LA name"),
                new Attribute(getProvider(), AttributeId.meridian_class_scale.name(), "meridian_class_scale"),
                new Attribute(getProvider(), AttributeId.wkt.name(), "Road segment Well Known Text geometry")
                );
    }

}
