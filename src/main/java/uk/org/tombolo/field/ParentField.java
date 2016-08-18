package uk.org.tombolo.field;

import java.util.List;

public interface ParentField extends Field {
    List<Field> getChildFields();
}
