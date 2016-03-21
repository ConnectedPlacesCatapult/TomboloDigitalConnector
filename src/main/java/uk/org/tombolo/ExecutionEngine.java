package uk.org.tombolo;

import java.io.File;

public interface ExecutionEngine {

	public void executeResource(String specificationResourcePath) throws Exception;
	
	public void execute(File specificationFile) throws Exception;
}
