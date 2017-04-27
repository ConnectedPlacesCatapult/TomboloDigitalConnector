package uk.org.tombolo.importer;

import uk.org.tombolo.core.SubjectType;

/**
 * Class representing the configuration file specified by the user.
 * Contains mandatory and optional fields.
 */

public class Config {
    private final int subjectIDIndex;
    private final String existingSubject;
    private final String provider;
    private final SubjectType subjectType;
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

    public SubjectType getSubjectType() {
        return subjectType;
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
        private final String provider;
        private final SubjectType subjectType;

        // Optional parameters with default values
        private String geographyProjection = "";
        private int geographyXIndex = -1;
        private int geographyYIndex = -1;

        public Builder(int subjectIDIndex, String existingSubject, String fileLocation, String provider, SubjectType subjectType) {
            this.subjectIDIndex = subjectIDIndex;
            this.existingSubject = existingSubject;
            this.fileLocation = fileLocation;
            this.provider = provider;
            this.subjectType = subjectType;
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
        subjectType = builder.subjectType;
        geographyProjection = builder.geographyProjection;
        geographyXIndex = builder.geographyXIndex;
        geographyYIndex = builder.geographyYIndex;
    }
}
