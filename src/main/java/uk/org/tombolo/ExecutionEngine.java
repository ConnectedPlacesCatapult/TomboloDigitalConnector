package uk.org.tombolo;

import uk.org.tombolo.execution.spec.DataExportSpecification;

import java.io.Writer;

public interface ExecutionEngine {
	public void execute(DataExportSpecification specification, Writer writer, boolean forceImport) throws Exception;
}
