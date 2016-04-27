package uk.org.tombolo.importer.ons;

import org.junit.BeforeClass;

import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractONSCensusImporterTest extends AbstractImporterTestUtils {

	protected static Importer importer;
	
	@BeforeClass
	public static void oneTimeSetUp(){
		importer = new ONSCensusImporter();
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
	
}
