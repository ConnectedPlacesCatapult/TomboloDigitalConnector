package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class SubjectSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;
    JSONObject matchRule;

    public SubjectSpecificationBuilder(String subjectType) {
        jsonSpec = new JSONObject();
        matchRule = new JSONObject();
        jsonSpec.put("subjectType", subjectType);
        jsonSpec.put("matchRule", matchRule);
    }

    public SubjectSpecificationBuilder setMatcher(String attribute, String pattern) {
        matchRule.put("attribute", attribute);
        matchRule.put("pattern", pattern);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }
}
