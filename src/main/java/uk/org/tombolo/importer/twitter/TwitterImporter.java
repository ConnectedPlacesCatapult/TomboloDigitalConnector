package uk.org.tombolo.importer.twitter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Class to import tweets from Twitter.
 *
 * One of the most common ways to get the tweets from Twitter is using the Stream API that will get the tweets real time
 * through a query. The query may contain constraints about the user, location, keywords etc.
 * Here the user can store in may ways, but we support the newline separated ones as it seems to be the common case.
 *
 * When wanting historical tweets Twitter provides the Search API that will get the tweets up to 1 week old.
 * Here the format is a proper JsonArray of statuses.
 *
 * Both approaches require some sort of tuning to avoid different problems like missing tweets, rate limits,
 * bot recognition as well as dealing with the fact that different problems need a large amount of tweets that require
 * continues crawling for different months. These are not included in this solution.
 * This importer considers that the user has already downloaded the data.
 *
 */
public class TwitterImporter extends GeneralImporter {

    private static final int SUBJECT_BUFFER_SIZE = 100000;

    private static org.slf4j.Logger log = LoggerFactory.getLogger(TwitterImporter.class);

    private DataSourceID dataSourceID;

    private Map<AttributeEnum, Attribute> map;
    private static Status status;
    private static Coordinate coordinate = null;

    private interface PropertyValue {
        String getValue();
    }

    private enum AttributeEnum implements PropertyValue {
        USER("user", "user screen name") {
            @Override
            public String getValue() {
                return status.getUser().getName();
            }
        },
        DESC("description", "user profile description") {
            @Override
            public String getValue() {
                return status.getUser().getDescription();
            }
        },
        LOCATION("location", "user location") {
            @Override
            public String getValue() {
                return status.getUser().getLocation();
            }
        },
        ACCOUNT_CREATION("account_creation", "user account creation date") {
            @Override
            public String getValue() {
                return status.getUser().getCreatedAt().toString();
            }
        },
        UTC_OFFSET("utc_offset", "offset from GMT/UTC in seconds") {
            @Override
            public String getValue() {
                return status.getUser().getUtcOffset() + "";
            }
        },
        FOLLOWERS("followers", "followers count") {
            @Override
            public String getValue() {
                return status.getUser().getFollowersCount() + "";
            }
        },
        FOLLOWING("following", "user friends count") {
            @Override
            public String getValue() {
                return status.getUser().getFriendsCount() + "";
            }
        },

        TEXT("text", "text, tweet content") {
            @Override
            public String getValue() {
                return status.getText();
            }
        },
        ID("id", "tweet ID") {
            @Override
            public String getValue() {
                return status.getId() + "";
            }
        },
        COORDINATES("coordinates", "tweet geolocation coordinates") {
            @Override
            public String getValue() {
                GeoLocation geoLocation = status.getGeoLocation();
                if (geoLocation != null) {
                    coordinate = new Coordinate(geoLocation.getLongitude(), geoLocation.getLatitude());
                    return coordinate.toString();
                }
                return "";
            }
        },
        TIMESTAMP("timestamp", "tweet creation time") {
            @Override
            public String getValue() {
                return status.getCreatedAt().toString();
            }
        },
        RETWEET("retweet", "retweet count") {
            @Override
            public String getValue() {
                return status.getRetweetCount() + "";
            }
        },
        SOURCE("source", "source, twitter client") {
            @Override
            public String getValue() {
                return status.getSource();
            }
        }
        ;

        private String name;
        private String desc;

        private AttributeEnum(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    public TwitterImporter(Config config) {
        super(config);

        dataSourceID = new DataSourceID(
                "Twitter",
                "",
                "Tweets from Twitter.com",
                "",
                ""
        );

        datasourceIds = Arrays.asList(dataSourceID.getLabel());
    }

    @Override
    public int getTimedValueBufferSize() {
        return SUBJECT_BUFFER_SIZE;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, String datasourceLocation) throws Exception {
        // Setting the timezone to UTC which is the timezone twitter bases the timestamps
        // Twitter4j will get the system settings so need to change them to get UTC
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);

        File file = new File(datasourceLocation);
        InputStream is = new FileInputStream(file);
        //Check if the file is gzipped
        boolean gzipped = ZipUtils.isGZipped(file);
        if (gzipped) { is = new GZIPInputStream(is); }

        // Search API output
        JsonReader reader = Json.createReader(is);
        JsonObject value = (JsonObject) reader.read();
        List statuses = value.getJsonArray("statuses");

        if (statuses == null) {
            is = new FileInputStream(file);
            if (gzipped) { is = new GZIPInputStream(is); }
            // Streaming API output, newline separated tweets
            String json = IOUtils.toString(is);
            statuses = Arrays.asList(json.split("[\r\n]"));
        }

        List<Subject> subjects = new ArrayList<>();
        List<FixedValue> fixedValues = new ArrayList<>();

        for (Object jsonValue: statuses) {
            String tweet = jsonValue.toString();
            try {
                status = TwitterObjectFactory.createStatus(tweet);
            } catch (TwitterException e) {
                log.error("Not a valid json string: {}, {}", tweet, e.getErrorMessage());
            }

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
            Geometry geometry = geometryFactory.createPoint(coordinate);

            Subject subject = new Subject(datasource.getUniqueSubjectType(),
                    AttributeEnum.ID.getValue(),
                    AttributeEnum.USER.getValue().replace(" ", "_") + AttributeEnum.ID.getValue(),
                    geometry
            );
            subjects.add(subject);

            for (AttributeEnum val : AttributeEnum.values()) {
                fixedValues.add(new FixedValue(subject, map.get(val), val.getValue()));
            }

            if (subjects.size() % getSubjectBufferSize() == 0) {
                saveAndClearSubjectBuffer(subjects);
                saveAndClearFixedValueBuffer(fixedValues);
            }
        }

        saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }

    @Override
    public Datasource getDatasource(Class<? extends Importer> importerClass, DataSourceID dataSourceID) throws Exception {
        return super.getDatasource(importerClass, dataSourceID);
    }

    @Override
    protected List<SubjectType> getSubjectTypes(DataSourceID dataSourceID) {
        return Arrays.asList(new SubjectType(getProvider(), "Tweet", "Tweet from Twitter"));

    }

    @Override
    protected List<Attribute> getFixedValuesAttributes(DataSourceID dataSourceID) {
        List<Attribute> attributes = new ArrayList<>();
        map = new HashMap<>();

        for (AttributeEnum val: AttributeEnum.values()) {
            Attribute attr = new Attribute(getProvider(), val.name,
                    val.name, val.desc, Attribute.DataType.string);
            attributes.add(attr);
            map.put(val, attr);
        }

        return attributes;
    }

    @Override
    protected void setupUtils(Datasource datasource) throws Exception {
    }

    @Override
    public Provider getProvider() {
        return new Provider("com.twitter", "Twitter");
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        if (dataSourceID.getLabel().equals(datasourceId)) {
            return getDatasource(getClass(), dataSourceID);
        } else {
            throw new ConfigurationException("Unknown datasourceID: " + datasourceId);
        }
    }
}
