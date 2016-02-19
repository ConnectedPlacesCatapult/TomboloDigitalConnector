package uk.org.tombolo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import uk.org.tombolo.reader.DataReader;
import uk.org.tombolo.reader.FeatureMatrix;
import uk.org.tombolo.writer.JsonDatapack;

public class Masher {
	public static enum OutputFormat {json,csv,weka}

	String localDataPath;
	
	public static void main(String[] args) {

		// FIXME: Read this in through the args ... handle both resource and normal paths
		String localDataPath = "/Users/bsigurbjornsson/Documents/Data/";

		//OutputFormat outputFormat = OutputFormat.json;
		//String resourcePath = "experiments/organicity_boroughs.json";
		//String outputPath = "organicity_boroughs.json";

		//OutputFormat outputFormat = OutputFormat.json;
		//String resourcePath = "experiments/organicity_neighbourhoods.json";
		//String outputPath = "organicity_neighbourhoods.json";
		
		OutputFormat outputFormat = OutputFormat.csv;
		String resourcePath = "experiments/obesity_uk.json";
		String outputPath   = "obesity_uk.csv";
				
		Masher masher = new Masher(localDataPath);
		try{
			masher.mashResource(resourcePath, outputFormat, outputPath);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public Masher(String localDataPath){
		this.localDataPath = localDataPath;
	}

	public void mashResource(String resourcePath, OutputFormat outputFormat, String outputPath) throws IOException, EncryptedDocumentException, InvalidFormatException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		mash(file, outputFormat, outputPath);
	}
	
	public void mash(File specification, OutputFormat outputFormat, String outputPath) throws IOException, EncryptedDocumentException, InvalidFormatException{
		DataReader dr = new DataReader(localDataPath);
		dr.readData(specification);
		FeatureMatrix matrix = dr.getFeatureMatrix();
		Writer writer = new FileWriter(outputPath);
		switch(outputFormat){
			case csv:
				matrix.writeCSV(writer);
				break;
			case weka:
				matrix.writeArff(writer);
				break;
			case json:
				JsonDatapack jsonDatapack = new JsonDatapack(matrix.getAttributes(), dr.getEntities());
				jsonDatapack.mergeFeatureMatrix(matrix);
				jsonDatapack.toJson(writer);
				break;
		}
		try {
			writer.flush();
			writer.close();
		} catch (IOException e){
			// Do nothing since the stream is already closed
		}
	}
	
}
