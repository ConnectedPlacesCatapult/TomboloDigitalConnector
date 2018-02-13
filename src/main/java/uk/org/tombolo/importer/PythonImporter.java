package uk.org.tombolo.importer;

import uk.org.tombolo.Py4jServer;
import uk.org.tombolo.Py4jServerInterface;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
Once the Py4jServer is running, importer built in python 
uses this class to pass the objects and save them in database.
*/
public class PythonImporter extends AbstractImporter {

    public void downloadData(String url, String dataCacheRootDirectory, String prefix, String suffix) throws IOException{
        DownloadUtils utils = new DownloadUtils(dataCacheRootDirectory);
        InputStream stream = utils.fetchInputStream(new URL(url), prefix, suffix);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String read;
        Py4jServerInterface serverInterface = (Py4jServerInterface) Py4jServer.server.getPythonServerEntryPoint(new Class[] {Py4jServerInterface.class});
        while ((read = reader.readLine()) != null) serverInterface.streamData(read);
    }

    public void saveProvider(Provider provider) {
        ProviderUtils.save(provider);
    }

    public void saveSubjectTypes(List<SubjectType> subjectTypes) {
        SubjectTypeUtils.save(subjectTypes);
    }

    public void saveAttributes(List<Attribute> attributes) {
        AttributeUtils.save(attributes);
    }

    @Override
    public void saveAndClearSubjectBuffer(List<Subject> subjectBuffer) {
        List<SubjectType> types = subjectBuffer.stream().map(Subject::getSubjectType).collect(Collectors.toList());
        SubjectTypeUtils.save(types);
        super.saveAndClearSubjectBuffer(subjectBuffer);
    }

    @Override
    public void saveAndClearTimedValueBuffer(List<TimedValue> timedValueBuffer) {
        List<Subject> subjects = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();
        timedValueBuffer.forEach(var -> {
            subjects.add(var.getId().getSubject());
            attributes.add(var.getId().getAttribute());
        });
        saveAndClearSubjectBuffer(subjects);
        saveAttributes(attributes);
        super.saveAndClearTimedValueBuffer(timedValueBuffer);
    }

    @Override
    public void saveAndClearFixedValueBuffer(List<FixedValue> fixedValueBuffer) {
        List<Subject> subjects = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();
        fixedValueBuffer.forEach(var -> {
            subjects.add(var.getId().getSubject());
            attributes.add(var.getId().getAttribute());
        });
        saveAndClearSubjectBuffer(subjects);
        saveAttributes(attributes);
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
