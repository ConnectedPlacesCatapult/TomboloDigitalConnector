package uk.org.tombolo.field.modelling;

import uk.org.tombolo.recipe.DatasourceRecipe;
import uk.org.tombolo.field.Field;

import java.util.List;

/**
 * Modelling fields are reusable recipes for calculating complex fields or models.
 * Uses include social resilience indices developed in collaboration with Leeds city council.
 */
public interface ModellingField extends Field {

    public List<DatasourceRecipe> getDatasources();
}
