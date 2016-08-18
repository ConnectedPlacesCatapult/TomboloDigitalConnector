package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class SubjectSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;
    public SubjectSpecificationBuilder(String subjectType) {
        jsonSpec = new JSONObject();
        jsonSpec.put("subjectType", subjectType);
    }

    public SubjectSpecificationBuilder setMatcher(String attribute, String pattern) {
        JSONObject matchRule = new JSONObject();
        matchRule.put("attribute", attribute);
        matchRule.put("pattern", pattern);
        jsonSpec.put("matchRule", matchRule);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }
}
