package uk.org.tombolo.recipe;

public class DataExportRecipe {

	// Recipe for the dataset to be exported
	DatasetRecipe dataset;
	// Class name to use when exporting
	// FIXME: We could include a syntactic-sugar such as "cvs", and "geojson" and then map that to class-name
	String exporter;

	public DatasetRecipe getDataset() {
		return dataset;
	}

	public void setDataset(DatasetRecipe dataset) {
		this.dataset = dataset;
	}

	public String getExporter() {
		return exporter;
	}

	public void setExporter(String exporter) {
		this.exporter = exporter;
	}
}
