package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

/**
 * Field.java
 * Fields take subjects and return some data.
 *
 * If you're thinking of a CSV file, fields are the columns. You can also think of them as
 * functions that map from Subjects to values. The job of Exporters is to take each Subject,
 * pass it through a set of Fields, and collect the outputs.
 *
 * Note that the Field is not required to use the Subject. See the FixedAnnotationField for
 * an example of this.
 *
 * Fields have different characteristics — for instance some fields may be able to return a
 * single value (e.g. a string), while others may only return structured data (e.g. a JSON
 * object). These are represented by interfaces that inherit from this one (e.g. SingleValueField).
 * Some exporters will only support certain kinds of Field (for instance, CSV files are tabular
 * and so can only accept SingleValueFields).
 *
 * Currently every Field must return some JSON, but this may change in future to some more
 * generic structured data type.
 */
public interface Field {
    JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException;
    String getLabel();
}
