package uk.org.tombolo;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.time.LocalDateTime;

public final class TestFactory {
    public static final Provider DEFAULT_PROVIDER = new Provider("default_provider_label", "default_provider_name");
    public static final String TIMESTAMP = "2011-01-01T00:00:00";

    private TestFactory() {}

    public static Attribute makeAttribute(Provider provider, String prefix) {
        ProviderUtils.save(provider);
        Attribute attribute = new Attribute(provider, prefix + "_label", prefix + "_name", prefix + "_description", null);
        AttributeUtils.save(attribute);
        return attribute;
    }

    public static TimedValue makeTimedValue(String geographyLabel, Attribute attribute, String timestamp, Double value) {
        Geography geography = GeographyUtils.getGeographyByLabel(geographyLabel);
        TimedValue timedValue = new TimedValue(geography, attribute, LocalDateTime.parse(timestamp), value);
        (new TimedValueUtils()).save(timedValue);
        return timedValue;
    }
}
