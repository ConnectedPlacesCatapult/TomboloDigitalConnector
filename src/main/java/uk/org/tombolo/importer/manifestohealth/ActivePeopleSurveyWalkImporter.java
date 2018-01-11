package uk.org.tombolo.importer.manifestohealth;

import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.manifestoenvironment.WasteImporter;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tbantis on 11/01/2018.
 */
public class ActivePeopleSurveyWalkImporter extends AbstractImporter {
    private static final String DATASOURCE = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536499/cw0105.ods";

    public ActivePeopleSurveyWalkImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("ActivePeopleWalk").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static final Provider PROVIDER = new Provider(
            "uk.gov.dft",
            "Department for Transport"
    );
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }


    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                ActivePeopleSurveyWalkImporter.class,
                "ActivePeopleWalk",
                "Proportion of how often and how long adults walk for (at least 10 minutes) by local authority",
                "Proportion of how often and how long adults walk for (at least 10 minutes) by local authority",
                DATASOURCE);
        return datasourceSpec;
    }


    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        String fileLocation = getDatasourceSpec("ActivePeopleWalk").getUrl();
        URL url;
        try {
            url = new URL(fileLocation);
        } catch (MalformedURLException e) {
            File file;
            if (!(file = new File(fileLocation)).exists()) {
                System.out.println("ERROR: File does not exist: " + fileLocation);
            }
            url = file.toURI().toURL();
        }
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".ods");

        //OpenDocument doc = new OpenDocument();
    }
}
