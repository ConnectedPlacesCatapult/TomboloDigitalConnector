package uk.org.tombolo.importer;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.ons.AbstractONSImporter;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AbstractOaImporterTest extends AbstractTest {
    AbstractImporterTest importer = new AbstractImporterTest();

    private class AbstractImporterTest extends AbstractOaImporter {

        @Override
        protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        }

        @Override
        public Provider getProvider() {
            return TestFactory.DEFAULT_PROVIDER;
        }

        @Override
        public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
            return new DatasourceSpec(this.getClass(), "testDatasourceId", "", "", "");
        }

        @Override
        public List<String> getDatasourceIds() {
            return Collections.singletonList("testDatasourceId");
        }

        @Override
        protected List<String> getOaDatasourceIds() {
            return Collections.singletonList("localAuthority");
        }
    }

    @Before
    public void setup() throws Exception {
        mockDownloadUtils(importer);
    }

    @Test
    public void getOaDatasourceIdsCounts() throws Exception {
        importer.importDatasource("testDatasourceId", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        //Test the importer imports only the subjects from OaImporter
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel()
                , "localAuthority");
        List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(subjectType, "%");
        assertEquals(7, subjects.size());

        assertEquals(0, importer.getTimedValueCount());
        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
    }
}
