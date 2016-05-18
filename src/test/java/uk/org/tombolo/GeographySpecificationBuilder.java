package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class GeographySpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;
    JSONArray matchers;

    public GeographySpecificationBuilder(String geographyType) {
        jsonSpec = new JSONObject();
        matchers = new JSONArray();
        jsonSpec.put("geographyType", geographyType);
        jsonSpec.put("matchers", matchers);
    }

    public GeographySpecificationBuilder addMatcher(String attribute, String pattern) {
        JSONObject matcher = new JSONObject();
        matcher.put("attribute", attribute);
        matcher.put("pattern", pattern);
        matchers.add(matcher);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }
}
