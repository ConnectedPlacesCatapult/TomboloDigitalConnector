package uk.org.tombolo.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.DataExportEngine;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InternalImporter extends AbstractImporter {
    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        File specFile = new File(ClassLoader.getSystemResource("executions/" + datasource.getLocalDatafile() + ".json").getFile());
        DataExportSpecification spec = SpecificationDeserializer.fromJsonFile(specFile, DataExportSpecification.class);
        StringWriter writer = new StringWriter();

        DataExportEngine engine = new DataExportEngine(this.properties, this.downloadUtils);
        engine.execute(spec, writer);
        return importGenerated(new StringReader(writer.toString()), spec.getDatasetSpecification().getFieldSpecification());
    }

    private int importGenerated(StringReader reader, List<FieldSpecification> fieldSpecs) throws IOException, ClassNotFoundException {
        int values = 0;
        CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
        Iterator<CSVRecord> iterator = parser.iterator();

        Provider provider = new Provider("uk.org.tombolo.internal", "Tombolo Internal");
        ProviderUtils.save(provider);
        List<TimedValue> timedValues = new ArrayList<>();

        while (iterator.hasNext()) {
            CSVRecord record = iterator.next();
            Subject subject = SubjectUtils.getSubjectByLabel(record.get("label"));
            for (FieldSpecification fieldSpec : fieldSpecs) {
                String fieldLabel = fieldSpec.toField().getLabel();
                Double value = Double.parseDouble(record.get(fieldLabel + "_latest_value"));
                LocalDateTime timestamp = LocalDateTime.now();
                Attribute attribute = new Attribute(provider, fieldLabel, fieldLabel, fieldLabel, null);
                AttributeUtils.save(attribute);
                timedValues.add(new TimedValue(subject, attribute, timestamp, value));
                values++;
            }
        }

        System.out.println(String.format("Saving %s values", values));
        TimedValueUtils.save(timedValues);

        return values;
    }

    @Override
    public Provider getProvider() {
        return null;
    }

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return null;
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        Datasource datasource = new Datasource("id", null, "name", "");
        datasource.setLocalDatafile(datasourceId);
        return datasource;
    }
}
