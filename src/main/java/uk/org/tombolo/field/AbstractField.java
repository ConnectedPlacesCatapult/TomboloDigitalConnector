package uk.org.tombolo.field;

import uk.org.tombolo.DataExportEngine;
import uk.org.tombolo.core.Subject;

public abstract class AbstractField implements Field {
    protected String label;

    public AbstractField(String label){
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    protected String getCachedValue(Subject subject){
        return DataExportEngine.getFieldCache().getChachedValue(this, subject);
    }

    protected void setCachedValue(Subject subject, String value){
        DataExportEngine.getFieldCache().putCachedValue(this, subject, value);
    }
}
