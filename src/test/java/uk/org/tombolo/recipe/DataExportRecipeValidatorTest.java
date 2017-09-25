package uk.org.tombolo.recipe;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.junit.Test;
import uk.org.tombolo.*;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataExportRecipeValidatorTest extends AbstractTest {

    @Test
    public void testValidateWithValidFixture() throws Exception {
        String resourcePath = "executions/valid_specification_file.json";
        ClassLoader classLoader = getClass().getClassLoader();
        ProcessingReport report = DataExportRecipeValidator.validate(new FileReader(classLoader.getResource(resourcePath).getFile()));
        assertTrue("Spec is valid", report.isSuccess());
    }

    @Test
    public void testValidateWithValidBuilder() throws Exception {
        DataExportSpecificationBuilder spec = DataExportSpecificationBuilder.withGeoJsonExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder("uk.gov.ons", "lsoa").setMatcher("label", "E01002766"))
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder("uk.gov.ons", "localAuthority").setMatcher("label", "E08000035"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW", "")
                .addFieldSpecification(
                        FieldBuilder.wrapperField("attributes", Arrays.asList(
                                FieldBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "CL_0000053_1") // total population
                        ))
                );
        ProcessingReport report = DataExportRecipeValidator.validate(new StringReader(spec.toJSONString()));
        assertTrue("Spec is valid", report.isSuccess());
    }

    @Test
    public void testValidateWithInvalidFixture() throws Exception {
        ProcessingReport report = DataExportRecipeValidator.validate(new StringReader("{}"));
        assertFalse("Spec is invalid", report.isSuccess());
    }
}