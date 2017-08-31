package uk.org.tombolo.importer.twitter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.*;

import javax.json.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Class to import tweets from Twitter.
 *
 * One of the most common ways to get the tweets from Twitter is using the Stream API that will get the tweets real time
 * through a query. The query may contain constraints about the user, location, keywords etc.
 * Here the user can store in many ways, but we support the newline separated ones as it seems to be the common case.
 *
 * When wanting historical tweets, Twitter provides the Search API that will get the tweets up to 1 week old.
 * Here the format is a proper JsonArray of statuses.
 *
 * Both approaches require some sort of tuning to avoid different problems like missing tweets, rate limits,
 * bot recognition as well as dealing with the fact that different problems need a large amount of tweets that require
 * continues crawling for different months. These are not included in this solution.
 * This importer considers that the user has already downloaded the data.
 *
 */
public class TwitterImporter extends AbstractImporter {

    private static final int SUBJECT_BUFFER_SIZE = 100000;

    private static org.slf4j.Logger log = LoggerFactory.getLogger(TwitterImporter.class);

    private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);

    private enum DatasourceId {
        twitter(new DatasourceSpec(
                TwitterImporter.class,
                "twitter",
                "",
                "Tweets from Twitter.com",
                "")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    private Map<AttributeEnum, Attribute> map;
    private static Status status;


    private List<Subject> subjects = new ArrayList<>();
    private List<FixedValue> fixedValues = new ArrayList<>();

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
                return status.getGeoLocation() + "";
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
        datasourceIds = Arrays.asList(DatasourceId.twitter.name());
    }

    @Override
    public int getTimedValueBufferSize() {
        return SUBJECT_BUFFER_SIZE;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // Setting the timezone to UTC which is the timezone twitter bases the timestamps
        // Twitter4j will get the system settings so need to change them to get UTC
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);

        for (String localdata : datasourceLocation) {
            File file = new File(localdata);
            InputStream is = new FileInputStream(file);
            //Check if the file is gzipped
            boolean gzipped = ZipUtils.isGZipped(file);
            if (gzipped) {
                is = new GZIPInputStream(is);
            }

            // First we try to check if it is a Search API out
            JsonReader reader = Json.createReader(is);
            JsonObject value = (JsonObject) reader.read();
            JsonArray statuses = value.getJsonArray("statuses");

            // The statuses list will be null if the file is not a Search API file
            if (statuses == null) {
                is = new FileInputStream(file);
                if (gzipped) {
                    is = new GZIPInputStream(is);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String tweet;
                while (!"".equals((tweet = br.readLine()))) {
                    subjectFromStatus(tweet, datasource);
                }
            } else {
                statuses.stream().forEach(jsonValue -> subjectFromStatus(jsonValue.toString(), datasource));
            }

            is.close();
        }

        saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }

    private void subjectFromStatus(String tweet, Datasource datasource) {
        try {
            status = TwitterObjectFactory.createStatus(tweet);
        } catch (TwitterException e) {
            log.error("Not a valid json string: {}, {}", tweet, e.getErrorMessage());
        }

        // Create status geometry
        GeoLocation geoLocation = status.getGeoLocation();
        Coordinate coordinate = null;
        // A null geoLocation will create an empty geometry
        if (geoLocation != null) {
            coordinate = new Coordinate(geoLocation.getLongitude(), geoLocation.getLatitude());
        }
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

    @Override
    public List<SubjectType> getDatasourceSubjectTypes(String dataSourceID) {
        return Arrays.asList(new SubjectType(getProvider(), "Tweet", "Tweet from Twitter"));

    }

    @Override
    public List<Attribute> getDatasourceFixedValueAttributes(String dataSourceID) {
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
    public Provider getProvider() {
        return new Provider("com.twitter", "Twitter");
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }
}
