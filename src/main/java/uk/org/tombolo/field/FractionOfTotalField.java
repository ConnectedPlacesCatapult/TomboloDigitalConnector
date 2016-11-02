package uk.org.tombolo.field;

import org.apache.commons.collections4.ListUtils;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
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
        obj.put("timestamp", valueWithTimestamp.timestamp.format(TimedValueId.DATE_TIME_FORMATTER));
        obj.put("value", valueWithTimestamp.value);
        return withinJsonStructure(obj);
    }

    private JSONObject withinJsonStructure(JSONAware values) {
        JSONObject obj = new JSONObject();
        obj.put(label, Collections.singletonList(values));
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    private ValueWithTimestamp getValue(Subject subject) throws IncomputableFieldException {
        List<TimedValue> dividendValues = getLatestTimedValuesForSubjectAndAttributes(subject, dividendAttributes);
        List<TimedValue> divisorValues = getLatestTimedValuesForSubjectAndAttributes(subject, Collections.singletonList(divisorAttribute));

        Double dividend = sumTimedValues(dividendValues);
        Double divisor = sumTimedValues(divisorValues);
        LocalDateTime latestTimeStamp = getMostRecentTimestampForTimedValues(ListUtils.union(dividendValues, divisorValues));

        if (0 == divisor) {
            throw new IncomputableFieldException("Cannot divide by zero");
        }

        return new ValueWithTimestamp(dividend/divisor, latestTimeStamp);
    }

    private List<TimedValue> getLatestTimedValuesForSubjectAndAttributes(Subject subject, List<AttributeMatcher> attributeMatchers) throws IncomputableFieldException {
        List<Attribute> attributes = getAttributes(attributeMatchers);
        List<TimedValue> timedValues = TimedValueUtils.getLatestBySubjectAndAttributes(subject, attributes);

        // We check for and throw on missing timedValues with some info on what they are
        if (timedValues.size() != attributeMatchers.size()) {
            List<Attribute> presentAttributes = timedValues.stream().map(timedValue -> timedValue.getId().getAttribute()).collect(Collectors.toList());
            List<Attribute> missingAttributes = ListUtils.subtract(attributes, presentAttributes);
            String missingAttributesString = missingAttributes.stream().map(Attribute::getLabel).collect(Collectors.joining(", "));
            throw new IncomputableFieldException(String.format("No TimedValue found for attributes %s", missingAttributesString));
        }

        return timedValues;
    }

    private List<Attribute> getAttributes(List<AttributeMatcher> attributeMatchers) {
        return attributeMatchers.stream().map(this::getAttribute).collect(Collectors.toList());
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
