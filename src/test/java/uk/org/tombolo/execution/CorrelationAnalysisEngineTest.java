package uk.org.tombolo.execution;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CorrelationAnalysisEngineTest {

    List<FieldRecipe> fields;

    @Before
    public void createFields(){
        fields = new ArrayList<>();
        fields.add(FieldBuilder.constantField("field1",null).build());
        fields.add(FieldBuilder.constantField("field2",null).build());
        fields.add(FieldBuilder.constantField("field3",null).build());
    }

    @Test
    public void readCSVDataExport() throws Exception {
        String filename = ClassLoader.getSystemResource("executions/correlation/dummyDataExport.csv").getPath();

        RealMatrix matrix = CorrelationAnalysisEngine.readCSVDataExport(filename,fields);

        assertEquals(3, matrix.getColumnDimension());
        assertEquals(2, matrix.getRowDimension());

        assertEquals(0.4d, matrix.getEntry(0,0), 0.1d);
        assertEquals(1d, matrix.getEntry(0,1), 0.1d);
        assertEquals(4d, matrix.getEntry(0,2), 0.1d);

        assertEquals(0.6d, matrix.getEntry(1,0), 0.1d);
        assertEquals(2d, matrix.getEntry(1,1), 0.1d);
        assertEquals(8d, matrix.getEntry(1,2), 0.1d);
    }

    @Test
    public void readGeoJsonDataExport() throws Exception {
        String filename = ClassLoader.getSystemResource("executions/correlation/dummyDataExport.json").getPath();

        RealMatrix matrix = CorrelationAnalysisEngine.readGeoJsonDataExport(filename,fields);

        assertEquals(3, matrix.getColumnDimension());
        assertEquals(2, matrix.getRowDimension());

        assertEquals(0.4d, matrix.getEntry(0,0), 0.1d);
        assertEquals(1d, matrix.getEntry(0,1), 0.1d);
        assertEquals(4d, matrix.getEntry(0,2), 0.1d);

        assertEquals(0.6d, matrix.getEntry(1,0), 0.1d);
        assertEquals(2d, matrix.getEntry(1,1), 0.1d);
        assertEquals(8d, matrix.getEntry(1,2), 0.1d);
    }

    @Test
    @Ignore // Ignoring this since this is basically just using the apache commons math analysis and writing to file
    public void calculateAndOutputCorrelations() throws Exception {

    }

}