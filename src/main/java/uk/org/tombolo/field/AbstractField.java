package uk.org.tombolo.field;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.FieldCache;
import uk.org.tombolo.field.value.FixedValueField;
import uk.org.tombolo.field.value.LatestValueField;
import uk.org.tombolo.field.value.ValuesByTimeField;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.stream.Collectors;

public abstract class AbstractField implements SingleValueField {
    protected String label;
    protected FieldCache fieldCache;
    private AttributeMatcher attribute;
    private Attribute cachedAttribute;
    private static String VALUE = "value";
    private static String TIMESTAMP = "timestamp";

    /*
    * Boolean object is used rather primitive boolean as
    * primitive boolean has default value of false and Object
    * Boolean has default of null.
    * */
    private Boolean timeStamp;

    public AbstractField(String label){
        this.label = label;
    }

    public AbstractField(String label, AttributeMatcher attributeMatcher) {
        this.label = label;
        this.attribute = attributeMatcher;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setFieldCache(FieldCache fieldCache){
        this.fieldCache = fieldCache;
    }

    protected String getCachedValue(Subject subject){
        if (fieldCache != null)
            return fieldCache.getChachedValue(this, subject);
        return null;
    }

    protected void setCachedValue(Subject subject, String value){
        if (fieldCache != null)
            fieldCache.putCachedValue(this, subject, value);
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        this.timeStamp = null == timeStamp ? true : timeStamp;
        JSONObject obj = new JSONObject();

        if (this instanceof ValuesByTimeField) {

            if (this instanceof FixedValueField) obj.put(VALUE, valueForSubject(subject, timeStamp));
            else if (this instanceof LatestValueField) {
                obj.put(TIMESTAMP, getTimedValue(subject).getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
                obj.put(VALUE, valueForSubject(subject, timeStamp));
            } else {
                JSONArray arr = new JSONArray();
                arr.addAll(TimedValueUtils.getBySubjectAndAttribute(subject, getAttribute()).stream().map(timedValue -> {
                    JSONObject pair = new JSONObject();
                    pair.put(TIMESTAMP, timedValue.getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
                    pair.put(VALUE, timedValue.getValue());
                    return pair;
                }).collect(Collectors.toList()));
                return withinMetadata(arr);
            }
            return metaData(obj);
        } else {
            String value = valueForSubject(subject, timeStamp);
            obj.put(label != null ? label : VALUE, isDouble(value) ? Double.valueOf(value) : value(value));
            return obj;
        }
    }

    private JSONObject metaData(JSONObject obj) {
        JSONArray values = new JSONArray();
        values.add(obj);
        return withinMetadata(values);
    }

    private boolean isDouble (String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Object value(String value) {
        try {
            Object o = new Gson().fromJson(value, JSONObject.class);
            return o;
        } catch (JsonSyntaxException e) {
            return value;
        }
    }

    @Override
    public TimedValue getTimedValue(Subject subject) throws IncomputableFieldException {
        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute());
        if (timedValue == null) {
            throw new IncomputableFieldException(String.format("No TimedValue found for attribute %s", getAttribute().getLabel()));
        }
        return timedValue;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getTimedValue(subject).getValue().toString();
    }

    protected JSONObject withinMetadata(JSONArray contents) {
        JSONObject obj = new JSONObject();
        JSONObject object = new JSONObject();

        if (!contents.isEmpty()) object = (JSONObject) contents.get(0);

        if (timeStamp) obj.put(label, contents);
        else {
            if (contents.size() < 2) obj.put(label, object.get("value"));
            else throw new JsonSyntaxException("Timestamp is required to be true");
        }

        return obj;
    }

    protected Attribute getAttribute() throws IncomputableFieldException {
        if (null != cachedAttribute) return cachedAttribute;

        Attribute attr = AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel);
        if (null == attr) {
            throw new IncomputableFieldException(String.format("No attribute found for provider %s and label %s", attribute.providerLabel, attribute.attributeLabel));
        } else {
            cachedAttribute = attr;
            return attr;
        }
    }

}
