
package uk.org.tombolo.field.value;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * TimeseriesMeanValueField.java
 * Returns the mean of all the values in a timeseries on an Attribute for a given Subject, plus metadata.
 *
 * The metadata is regarding the attribute.
 */
public class TimeseriesMeanValueField extends BasicValueField implements SingleValueField {
    private static Logger log = LoggerFactory.getLogger(TimeseriesMeanValueField.class);
    double meanValue = 0.0;
    public TimeseriesMeanValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return Double.toString(meanValue);
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONArray arr = new JSONArray();
        List<TimedValue> timedValue = TimedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        double meanValue = 0.0;
        int counterNull = 0;

        for (TimedValue value : timedValue) {
            if (null == value || null == value.getValue() || "".equals(value.getValue())) {
                counterNull++;
                continue;
            }
            JSONObject pair = new JSONObject();
            pair.put("timestamp", value.getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
            pair.put("value", value.getValue());
            meanValue += value.getValue();
            arr.add(pair);
        }

        if (arr.isEmpty()) log.warn("No Timed Value found for Subject {} ({})", subject.getName(), subject.getId());
        
        this.meanValue = meanValue / (timedValue.size() - counterNull);
        JSONObject obj = withinMetadata(arr);
        obj.put(label + " Mean", meanValue / (timedValue.size() - counterNull));
        return obj;
    }
}

