package uk.org.tombolo;

import java.io.File;

public interface ExecutionEngine {

	public void executeResource(String specificationResourcePath, boolean forceImport) throws Exception;
	
	public void execute(File specificationFile, boolean forceImport) throws Exception;
}
