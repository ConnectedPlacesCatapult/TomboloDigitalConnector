package uk.org.tombolo.field;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.FieldCache;

public abstract class AbstractField implements Field {
    protected String label;
    protected FieldCache fieldCache;

    public AbstractField(String label){
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setFieldCache(FieldCache fieldCache){
        this.fieldCache = fieldCache;
    }

    protected String getCachedValue(Subject subject){
        if (fieldCache != null)
            return fieldCache.getChachedValue(this, subject);
        return null;
    }

    protected void setCachedValue(Subject subject, String value){
        if (fieldCache != null)
            fieldCache.putCachedValue(this, subject, value);
    }

}
