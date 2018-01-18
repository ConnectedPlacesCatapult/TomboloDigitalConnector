package uk.org.tombolo.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.persistence.spi.ProviderUtil;
import uk.org.tombolo.Py4jServer;
import uk.org.tombolo.Py4jServerInterface;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;

public class PythonImporter extends AbstractImporter {

    public void downloadData(String url, String dataCacheRootDirectory, String prefix, String suffix) throws IOException{
        System.out.println("the url is : " + url);
        DownloadUtils utils = new DownloadUtils(dataCacheRootDirectory);
        InputStream stream = utils.fetchInputStream(new URL(url), prefix, suffix);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String read = "";
        Py4jServerInterface serverInterface = (Py4jServerInterface) Py4jServer.server.getPythonServerEntryPoint(new Class[] {Py4jServerInterface.class});
        while ((read = reader.readLine()) != null) {
            // System.out.println(read);
            serverInterface.streamData(read);
        }
    }

    public void saveProvider(Provider provider) {
        System.out.println("provider details are: " + provider.getLabel() + " " + provider.getName());
        // System.setProperty("user.dir", "/Users/hemanshu/Desktop/UptodateProject/TomboloDigitalConnector/src/main/resources");
        HibernateUtil.startUpForPython();
        ProviderUtils.save(provider);
    }

    public void saveSubjectTypes(List<SubjectType> subjectTypes) {
        SubjectTypeUtils.save(subjectTypes);
    }

    public void saveSubjects() {

    }

    public void saveAttributes(List<Attribute> attributes) {
        
    }

    @Override
    public void saveAndClearSubjectBuffer(List<Subject> subjectBuffer) {
        super.saveAndClearSubjectBuffer(subjectBuffer);
    }

    @Override
    public void saveAndClearTimedValueBuffer(List<TimedValue> timedValueBuffer) {
        super.saveAndClearTimedValueBuffer(timedValueBuffer);
    }

    @Override
    public void saveAndClearFixedValueBuffer(List<FixedValue> fixedValueBuffer) {
        super.saveAndClearFixedValueBuffer(fixedValueBuffer);
    }

	@Override
	public Provider getProvider() {
		return null;
	}

	@Override
	public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
		return null;
	}

	@Override
	protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope,
			List<String> datasourceLocation) throws Exception {
		
	}

    

}