package uk.org.tombolo.field.value;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TimeseriesField.java
 * Returns the timeseries on an Attribute for a given Subject, plus metadata.
 *
 * The metadata is regarding the attribute.
 */
public class TimeseriesField extends BasicValueField {

    public TimeseriesField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONArray arr = new JSONArray();
        List<TimedValue> timedValues = TimedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        if (timedValues.isEmpty()) {
            throw new IncomputableFieldException(String.format("No TimedValue found for attribute %s", getAttribute().getLabel()));
        }
        arr.addAll(timedValues.stream().map(timedValue -> {
            JSONObject pair = new JSONObject();
            pair.put("timestamp", timedValue.getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
            pair.put("value", timedValue.getValue());
            return pair;
            }).collect(Collectors.toList()));
        return withinMetadata(arr);
    }
}
