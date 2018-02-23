
package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.List;


public class TimeseriesMeanValueField extends BasicValueField implements SingleValueField {
    public TimeseriesMeanValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return Double.toString(getTimedValue(subject).getMeanValue());
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        MeanValueObject meanValue = getTimedValue(subject);
        JSONObject obj = new JSONObject();
        if (null != timeStamp && !timeStamp) {
            obj.put(null != this.label ? this.label : "value",
                                            meanValue.getMeanValue());
            return obj;
        }
        obj.put("timestamp", meanValue.getDateTimeRange());
        obj.put("value", meanValue.getMeanValue());
        return withinMetadata(obj);
    }

    private MeanValueObject getTimedValue(Subject subject) throws IncomputableFieldException {
        List<TimedValue> timedValue = TimedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        double addValue = 0.0;
        double meanValue = 0.0;
        StringBuilder range = new StringBuilder();
        String timeStamp;
        for (int i = 0; i < timedValue.size(); i++) {
            if (i == 0 || i == timedValue.size() - 1){
                range.append(timedValue.get(i).getId().getTimestamp()).append(" - ");
            }
            addValue += timedValue.get(i).getValue();
        }
        if (addValue > 0.0) {
            meanValue = addValue / timedValue.size();
        }

        timeStamp = range.toString();
        if (!timeStamp.isEmpty()) {
            timeStamp = timeStamp.substring(0, timeStamp.length() - 3);
        }

        return new MeanValueObject(timeStamp, meanValue);
    }

    public class MeanValueObject {
        private String dateTimeRange;
        private double meanValue;

        MeanValueObject(String dateTimeRange, double meanValue) {
            this.dateTimeRange = dateTimeRange;
            this.meanValue = meanValue;
        }

        String getDateTimeRange() {
            return dateTimeRange;
        }

        double getMeanValue() {
            return meanValue;
        }

        void setDateTimeRange(String dateTimeRange) {
            this.dateTimeRange = dateTimeRange;
        }

        void setMeanValue(double meanValue) {
            this.meanValue = meanValue;
        }

    }
}
