package uk.org.tombolo.transformer;

import java.util.List;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.TransformSpecification;

public interface Transformer {

	/**
	 * Transformation function that takes in a list of input attributes and transforms their values
	 * into list of values for the output attribute.
	 * 
	 *  Example: Input attributes may be the population count at certain age and the output attribute
	 *  might the fraction of the population that is 65 or older.
	 *
	 * @param subjects
	 * @param inputAttributes
	 * @param outputAttribute
	 * @return
	 */
	public List<TimedValue> transform(List<Subject> subjects, List<Attribute> inputAttributes, Attribute outputAttribute);

	public void transformBySpecification(List<Subject> subjects, TransformSpecification transformSpecification);

	public void setTimedValueUtils(TimedValueUtils timedValueUtils);
}
