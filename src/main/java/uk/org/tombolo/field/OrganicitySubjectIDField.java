package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

public class OrganicitySubjectIDField implements SingleValueField {
    private final String label;
    private final String site;
    private final String service;
    private final String provider;
    private final String group;

    public OrganicitySubjectIDField(String label, String site, String service, String provider, String group) {
        this.label = label;
        this.site = site;
        this.service = service;
        this.provider = provider;
        this.group = group;
    }

    @Override
    public String valueForSubject(Subject subject) {
        return "urn:oc:entity:"
                + site
                + (service != null  ? ":" + service : "")
                + (provider != null ? ":" + provider : "")
                + (group != null    ? ":" + group : "")
                + ":"
                + subject.getLabel();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        JSONObject obj = new JSONObject();
        obj.put(label, valueForSubject(subject));
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
}
