package uk.org.tombolo;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.util.List;

public class SubjectSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;
    public SubjectSpecificationBuilder(String provider, String subjectType) {
        jsonSpec = new JSONObject();
        jsonSpec.put("provider", provider);
        jsonSpec.put("subjectType", subjectType);
    }

    public SubjectSpecificationBuilder setMatcher(String attribute, String pattern) {
        JSONObject matchRule = new JSONObject();
        matchRule.put("attribute", attribute);
        matchRule.put("pattern", pattern);
        jsonSpec.put("matchRule", matchRule);
        return this;
    }

    public SubjectSpecificationBuilder setGeoMatcher(String geoRelation, List<SubjectSpecificationBuilder> subjectSpecifications){
        JSONObject geoMatchRule = new JSONObject();
        geoMatchRule.put("geoRelation", geoRelation);
        geoMatchRule.put("subjects", subjectSpecifications);
        jsonSpec.put("geoMatchRule", geoMatchRule);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }
}
