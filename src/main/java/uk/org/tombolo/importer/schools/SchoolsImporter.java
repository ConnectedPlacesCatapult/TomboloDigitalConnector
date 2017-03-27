package uk.org.tombolo.importer.schools;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.SingleValueExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.util.List;

/**
 * Class importing schools in England
 *
 * Data sourced here: https://www.gov.uk/government/publications/schools-in-england
 * NOTE the file containing the schools is updated monthly.
 */
public class SchoolsImporter extends AbstractSchoolsImporter {

    private enum schoolsDataSourceID implements DataSourceID {
        schoolsInEngland("Schools in England",
                "Schools in England",
                "https://www.gov.uk/government/publications/schools-in-england",
                "https://www.gov.uk/government/publications/schools-in-england/EduBase_Schools_February_2017.ods",
                1
        );

        private String name;
        private String description;
        private String url;
        private String remoteDataFile;
        private int sheet;

        private schoolsDataSourceID(String name, String description, String url, String remoteDataFile, int sheet) {
            this.name = name;
            this.description = description;
            this.url = url;
            this.remoteDataFile = remoteDataFile;
            this.sheet = sheet;
        }

        @Override
        public String getEnumConstantName() {
            return name();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getRemoteDataFile() {
            return remoteDataFile;
        }

        @Override
        public int getSheet() { return sheet; }
    }

    //This enum would be populated in case the dataset contains many irrelevant attributes
    // This enum allows to specify the attributes we are interested to.
    // In the specific case we are getting all the attributes from the dataset.
    private enum schoolsAttributeID implements AttributeID {
        ;

        private String name;
        private String description;
        private int columnID;
        private Attribute.DataType type;

        private schoolsAttributeID(String name, String description, int columnID, Attribute.DataType type) {
            this.name = name;
            this.description = description;
            this.columnID = columnID;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int columnID() {
            return columnID;
        }

        @Override
        public Attribute.DataType getType() {
            return type;
        }
    }

    private Range<Integer> sheetRange = Range.range(0, BoundType.CLOSED, 2, BoundType.CLOSED);
    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return datasourcesFromEnumeration(schoolsDataSourceID.class);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DataSourceID id;
        try {
            id = schoolsDataSourceID.valueOf(datasourceId);
        } catch (IllegalArgumentException e) {
            throw new Error("Unknown DataSourceID " + datasourceId);
        }
        return getDatasource(getClass(), id, schoolsAttributeID.class);
    }


    @Override
    protected <T extends Enum<T> & AttributeID> Object getExtractor(SingleValueExtractor subjectLabelExtractor, T attribute) {
        return new TimedValueExtractor(getProvider(), subjectLabelExtractor, new ConstantExtractor(""),
                new ConstantExtractor(""), new ConstantExtractor(""));
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        return 0;
    }
}
