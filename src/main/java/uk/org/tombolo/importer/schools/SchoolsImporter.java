package uk.org.tombolo.importer.schools;

import uk.org.tombolo.core.Datasource;

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
        DateTimeFormatter dft = DateTimeFormatter.ofPattern("MMMM_yyyy");
        LocalDate localDate = LocalDate.now();
        return dft.format(localDate).toString();
    }

    private enum schoolsDataSourceID implements DataSourceID {
        schools("Schools in England",
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
        return getDatasource(getClass(), id);
    }
}
