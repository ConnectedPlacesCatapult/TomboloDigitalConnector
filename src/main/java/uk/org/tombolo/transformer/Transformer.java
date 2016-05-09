package uk.org.tombolo.transformer;

import java.util.List;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.TimedValue;

public interface Transformer {

	/**
	 * Transformation function that takes in a list of input attributes and transforms their values
	 * into list of values for the output attribute.
	 * 
	 *  Example: Input attributes may be the population count at certain age and the output attribute
	 *  might the fraction of the population that is 65 or older.
	 * 
	 * @param inputAttribute
	 * @param outputAttribute
	 * @return
	 */
	public List<TimedValue> transform(List<Attribute> inputAttribute, Attribute outputAttribute);
		
}
