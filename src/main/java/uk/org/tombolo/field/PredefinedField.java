package uk.org.tombolo.field;

import uk.org.tombolo.execution.spec.DatasourceSpecification;

import java.util.List;

/**
 * Predefined fields are reusable recipes for calculating complex fields or models.
 * Uses include social resilience indices developed in collaboration with Leeds city council.
 */
public interface PredefinedField extends Field {

    public List<DatasourceSpecification> getDatasourceSpecifications();
}
