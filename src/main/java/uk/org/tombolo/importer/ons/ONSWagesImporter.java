package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;

import java.util.Collections;
import java.util.List;

/**
 * Importer for the ONS Wages data
 *
 * http://www.ons.gov.uk/employmentandlabourmarket/peopleinwork/earningsandworkinghours/datasets/placeofresidencebylocalauthorityashetable8
 */
public class ONSWagesImporter extends AbstractONSImporter implements Importer{

    private enum DatasourceId {laWages2016};
    private Datasource[] datasources = {
            new Datasource(
                    DatasourceId.laWages2016.name(),
                    getProvider(),
                    "Wages per Local Authority",
                    "Estimates of paid hours worked, weekly, hourly and annual earnings for UK employees by gender " +
                            "and full/part-time working by home based Region to Local Authority level.")
    };

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        return datasourcesFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);
        switch (datasourceId){
            case laWages2016:
                Datasource datasource = datasources[datasourceId.ordinal()];
                datasource.setUrl("http://www.ons.gov.uk/employmentandlabourmarket/" +
                        "peopleinwork/earningsandworkinghours/datasets/placeofresidencebylocalauthorityashetable8");
                datasource.setRemoteDatafile("http://www.ons.gov.uk/file?" +
                        "uri=/employmentandlabourmarket/peopleinwork/earningsandworkinghours/datasets/" +
                        "placeofresidencebylocalauthorityashetable8/2016/table82016provisional.zip");
                datasource.setLocalDatafile("/ONS/WagesTable82016provisional.zip");
                datasource.addAllTimedValueAttributes(getAttributes());
                return datasource;
            default:
                throw new Error("Unknown datasource");
        }
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        // FIXME: Import data when we finally manage to download this
        return 0;
    }

    private List<Attribute> getAttributes(){
        // FIXME: Output attributes when we finally manage to download this
        return Collections.emptyList();
    }
}
