package uk.org.tombolo.field;

public abstract class AbstractField implements Field {
    protected String label;

    public AbstractField(String label){
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}