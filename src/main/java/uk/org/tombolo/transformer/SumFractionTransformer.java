package uk.org.tombolo.transformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sun.source.tree.AnnotatedTypeTree;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class SumFractionTransformer extends AbstractTransformer implements Transformer {


	@Override
	public List<TimedValue> transform(List<Geography> geographies, List<Attribute> inputAttributes, Attribute outputAttribute) {
		List<TimedValue> values = new ArrayList<TimedValue>();

		for (Geography geography : geographies) {
			LocalDateTime localDateTime = LocalDateTime.MIN;
			double value = 0d;

			for (int i = 0; i < inputAttributes.size() - 1; i++) {
				Attribute attribute = inputAttributes.get(i);
				Optional<TimedValue> optionalTimedValue = timedValueUtils.getLatestByGeographyAndAttribute(geography, attribute);
				if (optionalTimedValue.isPresent()) {
					value += optionalTimedValue.get().getValue();
					if (optionalTimedValue.get().getId().getTimestamp().isAfter(localDateTime))
						localDateTime = optionalTimedValue.get().getId().getTimestamp();
				}
			}
			Attribute attribute = inputAttributes.get(inputAttributes.size()-1);
			Optional<TimedValue> timedValueOptional = timedValueUtils.getLatestByGeographyAndAttribute(geography, attribute);
			if (timedValueOptional.isPresent()) {
				value /= timedValueOptional.get().getValue();
				if (timedValueOptional.get().getId().getTimestamp().isAfter(localDateTime))
					localDateTime = timedValueOptional.get().getId().getTimestamp();
			}

			values.add(new TimedValue(geography, outputAttribute, localDateTime, value));
		}
		return values;
	}

}
