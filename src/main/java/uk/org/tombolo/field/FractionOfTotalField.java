package uk.org.tombolo.field;

import org.apache.commons.collections4.ListUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FractionOfTotalField.java
 * For a subject, returns the sum of its TimedValues for a list of dividend
 * attributes divided by a divisor attribute.
 */
public class FractionOfTotalField implements SingleValueField {
    private final String label;
    private final List<AttributeMatcher> dividendAttributes;
    private final AttributeMatcher divisorAttribute;
    private Map<AttributeMatcher, Attribute> cachedAttributes;

    private Logger log = LoggerFactory.getLogger(FractionOfTotalField.class);

    FractionOfTotalField(String label, List<AttributeMatcher> dividendAttributes, AttributeMatcher divisorAttribute) {
        this.label = label;
        this.dividendAttributes = dividendAttributes;
        this.divisorAttribute = divisorAttribute;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return getValue(subject).value.toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        ValueWithTimestamp valueWithTimestamp = getValue(subject);
        JSONObject obj = new JSONObject();
        obj.put(valueWithTimestamp.timestamp.toString(), valueWithTimestamp.value);
        return withinJsonStructure(obj);
    }

    private JSONObject withinJsonStructure(JSONObject valuesObj) {
        JSONObject obj = new JSONObject();
        JSONObject labelObj = new JSONObject();
        labelObj.put("values", valuesObj);
        obj.put(label, labelObj);
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return label;
    }

    private ValueWithTimestamp getValue(Subject subject) throws IncomputableFieldException {
        List<TimedValue> dividendValues = getLatestTimedValuesForSubjectAndAttributes(subject, dividendAttributes);
        List<TimedValue> divisorValues = getLatestTimedValuesForSubjectAndAttributes(subject, Collections.singletonList(divisorAttribute));

        Double dividend = sumTimedValues(dividendValues);
        Double divisor = sumTimedValues(divisorValues);
        LocalDateTime latestTimeStamp = getMostRecentTimestampForTimedValues(ListUtils.union(dividendValues, divisorValues));

        if (0 == divisor) {
            throw new IncomputableFieldException("Divisor cannot be zero or absent");
        } else if (0 == dividend) {
            throw new IncomputableFieldException("Dividend cannot be zero or absent");
        }

        return new ValueWithTimestamp(dividend/divisor, latestTimeStamp);
    }

    private List<TimedValue> getLatestTimedValuesForSubjectAndAttributes(Subject subject, List<AttributeMatcher> attributeMatchers) {
        TimedValueUtils timedValueUtils = new TimedValueUtils();
        return attributeMatchers.stream()
                .map(attributeMatcher -> {
                    return timedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute(attributeMatcher))
                            .orElseGet(() -> {
                                log.warn(String.format("No TimedValue found for attribute %s in field %s", attributeMatcher.attributeLabel, label));
                                return null;
                            });
                })
                .filter(timedValue -> null != timedValue)
                .collect(Collectors.toList());
    }

    private Double sumTimedValues(List<TimedValue> timedValues) {
        return timedValues.stream().mapToDouble(TimedValue::getValue).sum();
    }

    private LocalDateTime getMostRecentTimestampForTimedValues(List<TimedValue> timedValues) {
        return timedValues.stream()
                .map(timedValue -> timedValue.getId().getTimestamp())
                .max(LocalDateTime::compareTo)
                .orElseGet(() -> LocalDateTime.MIN);
    }

    protected Attribute getAttribute(AttributeMatcher attributeMatcher) {
        if (null == cachedAttributes) { cachedAttributes = new HashMap<>(); } // Gson will null this field whatever we do
        if (cachedAttributes.containsKey(attributeMatcher)) return cachedAttributes.get(attributeMatcher);

        Attribute attr = AttributeUtils.getByProviderAndLabel(attributeMatcher.providerLabel, attributeMatcher.attributeLabel);
        if (null == attr) {
            throw new IllegalArgumentException(String.format("No attribute found for provider %s and label %s", attributeMatcher.providerLabel, attributeMatcher.attributeLabel));
        } else {
            cachedAttributes.put(attributeMatcher, attr);
            return attr;
        }
    }

    private static final class ValueWithTimestamp {
        public final Double value;
        public final LocalDateTime timestamp;

        ValueWithTimestamp(Double value, LocalDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
