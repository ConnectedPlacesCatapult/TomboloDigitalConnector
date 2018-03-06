
package uk.org.tombolo.field.value;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.List;

/**
 * TimeseriesMeanValueField.java
 * Returns the mean of all the values in a timeseries on an Attribute for a given Subject, plus metadata.
 *
 * The metadata is regarding the attribute.
 */
public class TimeseriesMeanValueField extends BasicValueField implements SingleValueField {
    private double mean = 0.0;

    public TimeseriesMeanValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return Double.toString(mean);
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONArray arr = new JSONArray();
        List<TimedValue> timedValue = TimedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        if (timedValue.isEmpty()) {
            throw new IncomputableFieldException(String.format("No TimedValue found for attribute %s", getAttribute().getLabel()));
        }

        double sum = 0.0;
        int counterNonNull = 0;

        for (TimedValue value : timedValue) {
            if (null == value || null == value.getValue() || "".equals(value.getValue())) {
                continue;
            }
            JSONObject pair = new JSONObject();
            pair.put("timestamp", value.getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
            pair.put("value", value.getValue());
            sum += value.getValue();
            counterNonNull++;
            arr.add(pair);
        }

        mean = counterNonNull == 0 ? mean : sum / counterNonNull;
        JSONObject obj = withinMetadata(arr);
        obj.put(label + " Mean", mean);
        return obj;
    }
}

