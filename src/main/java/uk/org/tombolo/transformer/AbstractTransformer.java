package uk.org.tombolo.transformer;

import uk.org.tombolo.core.utils.TimedValueUtils;

/**
 * Created by bsigurbjornsson on 10/05/2016.
 */
public abstract class AbstractTransformer implements Transformer {

    TimedValueUtils timedValueUtils;

    @Override
    public void setTimedValueUtils(TimedValueUtils timedValueUtils) {
        this.timedValueUtils = timedValueUtils;
    }
}
