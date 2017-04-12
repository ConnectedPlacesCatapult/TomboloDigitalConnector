package uk.org.tombolo.importer;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;

import java.util.List;

/**
 * General Importer class
 */
public abstract class GeneralImporter extends AbstractImporter {

    public Datasource getDatasource(Class<? extends Importer> importerClass, DataSourceID dataSourceID) throws Exception {
        Datasource dataSource = new Datasource(importerClass,
                dataSourceID.getLabel(),
                getProvider(),
                dataSourceID.getName(),
                dataSourceID.getDescription()
        );
        dataSource.setUrl(dataSourceID.getUrl());
        dataSource.setRemoteDatafile(dataSourceID.getRemoteDataFile());
        dataSource.setLocalDatafile(dataSourceID.getLocalDataFile());

        dataSource.addSubjectType(getSubjectType(dataSourceID));

        setupUtils(dataSource);

        dataSource.addAllFixedValueAttributes(getFixedValuesAttributes(dataSourceID));

        return dataSource;
    }

    protected abstract SubjectType getSubjectType(DataSourceID dataSourceID);

    protected abstract List<Attribute> getFixedValuesAttributes(DataSourceID dataSourceID);

    protected abstract void setupUtils(Datasource datasource) throws Exception;

}
