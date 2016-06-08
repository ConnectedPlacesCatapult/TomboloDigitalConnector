package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class SubjectSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;
    JSONArray matchers;

    public SubjectSpecificationBuilder(String subjectType) {
        jsonSpec = new JSONObject();
        matchers = new JSONArray();
        jsonSpec.put("subjectType", subjectType);
        jsonSpec.put("matchers", matchers);
    }

    public SubjectSpecificationBuilder addMatcher(String attribute, String pattern) {
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
