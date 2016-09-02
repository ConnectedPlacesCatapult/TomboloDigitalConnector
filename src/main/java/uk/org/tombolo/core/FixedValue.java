package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="fixed_value")
public class FixedValue {
    @EmbeddedId
    FixedValueId id;

    @Column(name="value")
    Double value;

    public FixedValue(){

    }

    public FixedValue(Subject subject, Attribute attribute, Double value){
        this.id = new FixedValueId(subject, attribute);
        this.value = value;
    }

    public FixedValueId getId() {
        return id;
    }

    public void setId(FixedValueId id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass()
                && id.equals(((FixedValue) obj).getId());
    }
}