package uk.org.tombolo;

import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.importer.ImporterMatcher;

import java.io.Writer;

public interface ExecutionEngine {
	public void execute(DataExportSpecification specification, Writer writer) throws Exception;
	public void execute(DataExportSpecification specification, Writer writer, ImporterMatcher forceImports) throws Exception;
}
