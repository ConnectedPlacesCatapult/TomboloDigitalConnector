package uk.org.tombolo.importer;

import org.opengis.feature.simple.SimpleFeature;
import uk.org.tombolo.core.SubjectType;

/**
 * This interface contains some extra methods needed when importing shapefiles.
 */
public interface ShapefileImporter {

    String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType);

    String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType);
}
