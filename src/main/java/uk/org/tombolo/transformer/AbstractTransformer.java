package uk.org.tombolo.transformer;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.TransformSpecification;

import java.util.List;

/**
 *
 */
public abstract class AbstractTransformer implements Transformer {

    TimedValueUtils timedValueUtils;

    @Override
    public void setTimedValueUtils(TimedValueUtils timedValueUtils) {
        this.timedValueUtils = timedValueUtils;
    }

    public void transformBySpecification(List<Subject> geographies, TransformSpecification transformSpecification) {
        List<Attribute> inputAttributes = transformSpecification.getInputAttributes();
        Attribute outputAttribute = transformSpecification.getOutputAttribute();
        ProviderUtils.save(outputAttribute.getProvider());
        AttributeUtils.save(outputAttribute);
        List<TimedValue> timedValues = transform(geographies, inputAttributes, outputAttribute);
        timedValueUtils.save(timedValues);
    }
}
