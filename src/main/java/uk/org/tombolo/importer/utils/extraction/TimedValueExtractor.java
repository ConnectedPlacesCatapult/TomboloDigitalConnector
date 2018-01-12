package uk.org.tombolo.importer.utils.extraction;

import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ParsingException;

import java.time.LocalDateTime;

public class TimedValueExtractor {
    private static final Logger log = LoggerFactory.getLogger(TimedValueExtractor.class);

    private Provider provider;
    private SubjectType subjectType;
    private SingleValueExtractor subjectLabelExtractor;
    private SingleValueExtractor attributeLabelExtractor;
    private SingleValueExtractor timestampExtractor;
    private SingleValueExtractor valueExtractor;

    public TimedValueExtractor(
            Provider provider,
            SubjectType subjectType,
            SingleValueExtractor subjectLabelExtractor,
            SingleValueExtractor attributeLabelExtractor,
            SingleValueExtractor timestampExtractor,
            SingleValueExtractor valueExtractor){
        this.provider = provider;
        this.subjectType = subjectType;
        this.subjectLabelExtractor = subjectLabelExtractor;
        this.attributeLabelExtractor = attributeLabelExtractor;
        this.timestampExtractor = timestampExtractor;
        this.valueExtractor = valueExtractor;
    }

    public TimedValue extract() throws ExtractorException {
        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, subjectLabelExtractor.extract());
        if (subject == null)
            throw new UnknownSubjectLabelException("Unknown subject: "+subjectLabelExtractor.extract()+"("+subjectType.getLabel()+")");
        Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeLabelExtractor.extract());
        if (attribute == null)
            throw new ExtractorException("Unknown attribute: "+attributeLabelExtractor.extract());
        LocalDateTime timestamp;
        try {
            timestamp = TimedValueUtils.parseTimestampString(timestampExtractor.extract());
        } catch (ParsingException e) {
            throw new ExtractorException("Unparsable timestamp: " + timestampExtractor.extract());
        }
        String valueString = valueExtractor.extract();
        // Parsing out proper numbers
        valueString = valueString.replaceAll("[^\\d]+(-?\\d+\\.?\\d+)[^\\d]+", "$1");
        Double value =  Double.parseDouble(valueString);
        if (value == null)
            throw new ExtractorException("Unparsable number: " + valueExtractor.extract());
        return new TimedValue(subject, attribute, timestamp, value);
    }

    public SingleValueExtractor getSubjectLabelExtractor() {
        return subjectLabelExtractor;
    }
    public SingleValueExtractor getAttributeLabelExtractor() {
        return attributeLabelExtractor;
    }
    public SingleValueExtractor getTimestampExtractor() {
        return timestampExtractor;
    }
    public SingleValueExtractor getValueExtractor() {
        return valueExtractor;
    }

    public void setRow(Row row){
        if (subjectLabelExtractor instanceof RowCellExtractor)
            ((RowCellExtractor) subjectLabelExtractor).setRow(row);
        if (attributeLabelExtractor instanceof RowCellExtractor)
            ((RowCellExtractor) attributeLabelExtractor).setRow(row);
        if (timestampExtractor instanceof RowCellExtractor)
            ((RowCellExtractor) timestampExtractor).setRow(row);
        if (valueExtractor instanceof RowCellExtractor)
            ((RowCellExtractor) valueExtractor).setRow(row);
    }
}
