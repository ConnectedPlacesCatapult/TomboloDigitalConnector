package uk.org.tombolo.importer;

/**
 * Class representing the configuration file specified by the user.
 * Contains mandatory and optional fields.
 */

public class Config {
    private final int subjectIDIndex;
    private final String existingSubject;
    private final String provider;
    private final String subjectTypeProvider;
    private final String subjectTypeLabel;
    private final String geographyProjection;
    private final int geographyXIndex;
    private final int geographyYIndex;
    private final String fileLocation;

    public int getSubjectIDIndex() {
        return subjectIDIndex;
    }

    public String getExistingSubject() {
        return existingSubject;
    }

    public String getProvider() {
        return provider;
    }

    public String getSubjectTypeProvider() {
        return subjectTypeProvider;
    }

    public String getSubjectTypeLabel() {
        return subjectTypeLabel;
    }

    public String getGeographyProjection() {
        return geographyProjection;
    }

    public int getGeographyXIndex() {
        return geographyXIndex;
    }

    public int getGeographyYIndex() {
        return geographyYIndex;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public static class Builder {
        // Required parameters
        private final int subjectIDIndex;
        private final String existingSubject;
        private final String fileLocation;

        // Optional parameters with default values
        private String provider = "";
        private String subjectTypeProvider = "";
        private String subjectTypeLabel = "";
        private String geographyProjection = "";
        private int geographyXIndex = -1;
        private int geographyYIndex = -1;

        public Builder(int subjectIDIndex, String existingSubject, String fileLocation) {
            this.subjectIDIndex = subjectIDIndex;
            this.existingSubject = existingSubject;
            this.fileLocation = fileLocation;
        }

        public Builder newSubject(String provider, String subjectTypeProvider, String subjectTypeLabel) {
            this.provider = provider;
            this.subjectTypeProvider = subjectTypeProvider;
            this.subjectTypeLabel = subjectTypeLabel;

            return this;
        }

        public Builder geography(String geographyProjection, int geographyXIndex, int geographyYIndex) {
            this.geographyProjection = geographyProjection;
            this.geographyXIndex = geographyXIndex;
            this.geographyYIndex = geographyYIndex;

            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }

    private Config(Builder builder) {
        subjectIDIndex = builder.subjectIDIndex;
        existingSubject = builder.existingSubject;
        fileLocation = builder.fileLocation;
        provider = builder.provider;
        subjectTypeProvider = builder.subjectTypeProvider;
        subjectTypeLabel = builder.subjectTypeLabel;
        geographyProjection = builder.geographyProjection;
        geographyXIndex = builder.geographyXIndex;
        geographyYIndex = builder.geographyYIndex;
    }
}
