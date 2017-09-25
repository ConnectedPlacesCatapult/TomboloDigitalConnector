package uk.org.tombolo.execution;

import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.importer.ImporterMatcher;

import java.io.Writer;

public interface ExecutionEngine {
	public void execute(DataExportRecipe specification, Writer writer) throws Exception;
	public void execute(DataExportRecipe specification, Writer writer, ImporterMatcher forceImports) throws Exception;
}
