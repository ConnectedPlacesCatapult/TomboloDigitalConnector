package uk.org.tombolo.importer.schools;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.SingleValueExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Class importing schools in England
 *
 * Data sourced here: https://www.gov.uk/government/publications/schools-in-england
 * NOTE the file containing the schools is updated monthly.
 */
public class SchoolsImporter extends AbstractSchoolsImporter {

    private static String getFormattedMonthYear() {
        DateTimeFormatter dft = DateTimeFormatter.ofPattern("MMMMM_yyyy");
        LocalDate localDate = LocalDate.now();
        return dft.format(localDate).toString();
    }

    private enum schoolsDataSourceID implements DataSourceID {
        schoolsInEngland("Schools in England",
                "Schools in England",
                "https://www.gov.uk/government/publications/schools-in-england/",
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/597965/EduBase_Schools_" + getFormattedMonthYear() + ".xlsx",
                "EduBase_Schools_March_2017.xlsx",
                0
        );

        private String name;
        private String description;
        private String url;
        private String remoteDataFile;
        private String localDataFile;
        private int sheet;

        private schoolsDataSourceID(String name, String description, String url, String remoteDataFile, String localDataFile, int sheet) {
            this.name = name;
            this.description = description;
            this.url = url;
            this.remoteDataFile = remoteDataFile;
            this.localDataFile = localDataFile;
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
        public String getLocalDataFile() { return localDataFile; }

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
    @Deprecated
    protected <T extends Enum<T> & AttributeID> Object getExtractor(SingleValueExtractor subjectLabelExtractor, T attribute) {
        return new TimedValueExtractor(getProvider(), subjectLabelExtractor, new ConstantExtractor(""),
                new ConstantExtractor(""), new ConstantExtractor(""));
    }
}
