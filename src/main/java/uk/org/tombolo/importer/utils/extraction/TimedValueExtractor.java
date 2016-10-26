package uk.org.tombolo.importer.utils.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.time.LocalDateTime;

public class TimedValueExtractor {
    private static final Logger log = LoggerFactory.getLogger(TimedValueExtractor.class);

    private Provider provider;
    private SingleValueExtractor subjectLabelExtractor;
    private SingleValueExtractor attributeLabelExtractor;
    private SingleValueExtractor timestampExtractor;
    private SingleValueExtractor valueExtractor;

    public TimedValueExtractor(
            Provider provider,
            SingleValueExtractor subjectLabelExtractor,
            SingleValueExtractor attributeLabelExtractor,
            SingleValueExtractor timestampExtractor,
            SingleValueExtractor valueExtractor){
        this.provider = provider;
        this.subjectLabelExtractor = subjectLabelExtractor;
        this.attributeLabelExtractor = attributeLabelExtractor;
        this.timestampExtractor = timestampExtractor;
        this.valueExtractor = valueExtractor;
    }

    public TimedValue extract() throws ExtractorException {
        Subject subject = SubjectUtils.getSubjectByLabel(subjectLabelExtractor.extract());
        if (subject == null)
            throw new UnknownSubjectLabelException("Unknown subject: "+subjectLabelExtractor.extract());
        Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeLabelExtractor.extract());
        if (attribute == null)
            throw new ExtractorException("Unknown attribute: "+attributeLabelExtractor.extract());
        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(timestampExtractor.extract());
        if (timestamp == null)
            throw new ExtractorException("Unparsable timestamp: " + timestampExtractor.extract());
        Double value = Double.valueOf(valueExtractor.extract());
        if (value == null)
            throw new ExtractorException("Unparsable number: " + valueExtractor.extract());
        return new TimedValue(subject, attribute, timestamp, value);
    }

    public SingleValueExtractor getValueExtractor() {
        return valueExtractor;
    }
}
