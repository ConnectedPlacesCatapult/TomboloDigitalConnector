package uk.org.tombolo.field;

import uk.org.tombolo.execution.spec.DatasourceSpecification;

import java.util.List;

public interface PredefinedField {

    public List<DatasourceSpecification> getDatasourceSpecifications();
}
