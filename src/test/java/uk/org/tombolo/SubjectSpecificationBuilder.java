package uk.org.tombolo;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.SubjectSpecification;

import java.util.List;

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

    public SubjectSpecificationBuilder setGeoMatcher(String geoRelation, List<SubjectSpecificationBuilder> subjectSpecifications){
        JSONObject geoMatchRule = new JSONObject();
        geoMatchRule.put("geoRelation", geoRelation);
        geoMatchRule.put("subjectSpecifications", subjectSpecifications);
        jsonSpec.put("geoMatchRule", geoMatchRule);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }
}
