package uk.org.tombolo.core.utils;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.DataExportSpecificationBuilder;
import uk.org.tombolo.SubjectSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpatialJoinTest extends AbstractTest {

    @Test
    public void testSpatialJoins(){
        SubjectType subjectType1 = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "subjectType1", "");
        SubjectType subjectType2 = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "subjectType1", "");

        Coordinate[] coordinates1 = {new Coordinate(0, 0), new Coordinate(5,5), new Coordinate(10,10)};
        Coordinate[] coordinates2 = {new Coordinate(0, 0), new Coordinate(10,10)};
        Coordinate[] coordinates3 = {new Coordinate(0, 0), new Coordinate(5,5), new Coordinate(0,10)};
        Subject subject1 = TestFactory.makeSubject(subjectType1, "line00-55-1010","",
                TestFactory.makeLineStringGeometry(coordinates1));
        TestFactory.makeSubject(subjectType2, "line00-1010","", TestFactory.makeLineStringGeometry(coordinates2));
        TestFactory.makeSubject(subjectType2, "line00-55-010","", TestFactory.makeLineStringGeometry(coordinates3));
        TestFactory.makeSubject(subjectType2, "point05","", TestFactory.makePointGeometry(0d, 5d));
        TestFactory.makeSubject(subjectType2, "point00","", TestFactory.makePointGeometry(0d, 0d));
        TestFactory.makeSubject(subjectType2, "square","",
                TestFactory.makeSquareGeometry(8d, 0d, 11d));

        SubjectSpecificationBuilder parentSpec = new SubjectSpecificationBuilder(
                TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType1.getLabel())
                .setMatcher("label", subject1.getLabel());
        List<SubjectSpecificationBuilder> parentSpecs = new ArrayList<>();
        parentSpecs.add(parentSpec);

        // Equals: Returns true if the given geometries represent the same geometry. Directionality is ignored.
        SubjectRecipe childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType2.getLabel())
                        .setGeoMatcher("equals", parentSpecs)).build().getDataset().getSubjects().get(0);

        List<Subject> subjects = SubjectUtils.getSubjectBySpecification(childSpec);

        // Matches line00-55-1010 and line00-1010 as equals includes also the parent subject itself
        assertEquals(2, subjects.size());

        // Disjoint: Returns TRUE if the Geometries do not "spatially intersect" - if they do not share any space together.
        childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType2.getLabel())
                        .setGeoMatcher("disjoint", parentSpecs)).build().getDataset().getSubjects().get(0);
        subjects = SubjectUtils.getSubjectBySpecification(childSpec);

        // Parent subject line00-55-1010 does not share any space only with point05 instead with line00-1010 and line00-55-010 yes
        assertEquals(1, subjects.size());

        // Touches:  Returns TRUE if the geometries have at least one point in common, but their interiors do not intersect.
        childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType2.getLabel())
                        .setGeoMatcher("touches", parentSpecs)).build().getDataset().getSubjects().get(0);
        subjects = SubjectUtils.getSubjectBySpecification(childSpec);

        // Parent subject line00-55-1010 touches only with point00
        assertEquals(1, subjects.size());

        // Intersects: Returns TRUE if the Geometries/Geography "spatially intersect in 2D" - (share any portion of space)
        // and FALSE if they don't (they are Disjoint). For geography -- tolerance is 0.00001 meters
        // (so any points that close are considered to intersect)
        childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType2.getLabel())
                        .setGeoMatcher("intersects", parentSpecs)).build().getDataset().getSubjects().get(0);
        subjects = SubjectUtils.getSubjectBySpecification(childSpec);
        // Parent subject line00-55-1010 shares space, so intersects with point00, line00-1010, line00-55-010, square
        // and itself
        assertEquals(5, subjects.size());

        // Crosses: Returns TRUE if the supplied geometries have some, but not all, interior points in common.
        childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType2.getLabel())
                        .setGeoMatcher("crosses", parentSpecs)).build().getDataset().getSubjects().get(0);
        subjects = SubjectUtils.getSubjectBySpecification(childSpec);

        // Parent subject line00-55-1010 crosses square
        assertEquals(1, subjects.size());

        // Overlaps: Returns TRUE if the Geometries share space, are of the same dimension,
        // but are not completely contained by each other.
        childSpec = DataExportSpecificationBuilder.withCSVExporter().addSubjectSpecification(
                new SubjectSpecificationBuilder(TestFactory.DEFAULT_PROVIDER.getLabel(), subjectType2.getLabel())
                        .setGeoMatcher("overlaps", parentSpecs)).build().getDataset().getSubjects().get(0);
        subjects = SubjectUtils.getSubjectBySpecification(childSpec);

        // Parent subject line00-55-1010 shares space with more, but overlaps only with line00-55-010 as they are of
        // the same dimension.
        assertEquals(1, subjects.size());
    }
}
