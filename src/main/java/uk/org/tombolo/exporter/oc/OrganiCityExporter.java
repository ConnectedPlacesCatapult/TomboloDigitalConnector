package uk.org.tombolo.exporter.oc;

import java.io.Writer;
import java.util.List;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyTypeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.exporter.Exporter;

public class OrganiCityExporter implements Exporter {

	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {

		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
			List<Geography> geographyList = GeographyUtils
					.getGeographyByTypeAndLabelPattern(geographyType, geographySpecification.getLabelPattern());
			for (Geography geography : geographyList){

				// TODO Write Geography info

				
				// TODO Write Attributes
				List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
				for (AttributeSpecification attributeSpec : attributeSpecs){
					Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
					Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());
					
					// TODO Write TimedValues

					
				}				
			}
		}
	}
}
