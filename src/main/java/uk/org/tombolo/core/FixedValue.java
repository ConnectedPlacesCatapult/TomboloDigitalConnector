package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="fixed_value")
public class FixedValue {
    @EmbeddedId
    FixedValueId id;

    @Column(name="value")
    String value;

    public FixedValue(){

    }

    public FixedValue(Subject subject, Attribute attribute, String value){
        this.id = new FixedValueId(subject, attribute);
        this.value = value;
    }

    public FixedValueId getId() {
        return id;
    }

    public void setId(FixedValueId id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass()
                && id.equals(((FixedValue) obj).getId());
    }
}