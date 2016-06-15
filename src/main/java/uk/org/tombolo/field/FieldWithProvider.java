package uk.org.tombolo.field;

import uk.org.tombolo.core.Provider;

/**
 * FieldWithProvider.java
 * A field that possesses some Provider object.
 *
 * This is here for backwards compatibility and will be removed in future
 * in favour of more specific Fields.
 */

@Deprecated
public interface FieldWithProvider extends Field {
    Provider getProvider();
}
