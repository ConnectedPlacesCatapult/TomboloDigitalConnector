package uk.org.tombolo.core;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class FixedValueId implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name="subject_id")
    Subject subject;

    @ManyToOne
    @JoinColumn(name="attribute_id")
    Attribute attribute;

    public FixedValueId(){

    }

    public FixedValueId(Subject subject, Attribute attribute){
        this.subject = subject;
        this.attribute = attribute;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public int hashCode() {
        return (subject.getId()
                +"\t"+attribute.getId()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;

        FixedValueId idObj = (FixedValueId)obj;
        if (idObj.subject.equals(this.subject)
                && idObj.attribute.equals(this.attribute))
            return true;

        return false;
    }
}