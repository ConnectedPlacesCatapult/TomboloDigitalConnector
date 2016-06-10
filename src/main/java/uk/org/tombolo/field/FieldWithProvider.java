package uk.org.tombolo.field;

import uk.org.tombolo.core.Provider;

public interface FieldWithProvider extends Field {
    Provider getProvider();
}
