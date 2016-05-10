package uk.org.tombolo.transformer;

import uk.org.tombolo.core.utils.TimedValueUtils;

/**
 *
 */
public abstract class AbstractTransformer implements Transformer {

    TimedValueUtils timedValueUtils;

    @Override
    public void setTimedValueUtils(TimedValueUtils timedValueUtils) {
        this.timedValueUtils = timedValueUtils;
    }
}
