package uk.org.tombolo.importer;

import org.geotools.data.DataStore;
import org.opengis.feature.simple.SimpleFeature;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;

import java.io.IOException;
import java.util.Map;

/**
 * This interface contains some extra methods needed when importing Geotools DataStores.
 */
public interface GeotoolsDataStoreImporter {

    SubjectType getSubjectType();

    String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType);

    String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType);

    String getEncoding();

    Map<String, Object> getParamsForDatasource(Datasource datasource);

    DataStore getDataStoreForDatasource(Datasource datasource) throws IOException;
}
