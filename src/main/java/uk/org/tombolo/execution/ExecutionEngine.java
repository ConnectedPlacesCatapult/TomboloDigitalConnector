package uk.org.tombolo.execution;

import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.importer.ImporterMatcher;

import java.io.Writer;

public interface ExecutionEngine {
	void execute(DataExportRecipe specification, Writer writer, ImporterMatcher forceImports) throws Exception;
}
