package uk.org.tombolo.recipe;

import java.util.List;

public class AttributeMatcher {
    public final String provider;
    public final String label;
    public final List<String> values;

    public AttributeMatcher(String provider, String label, List<String> values) {
        this.provider = provider;
        this.label = label;
        this.values = values;
    }
}
