package uk.org.tombolo.importer.manifestohealth;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tbantis on 11/12/2017.
 */
public class NCMPImporter extends AbstractImporter {

    private static final String DATASOURCE = "/Users/tbantis/Desktop/Copy of NCMP_data_LA_and_England.xlsx";

    // A standard importer inherits the below classes from AbstractImporter

    // This config file is an inheritance for a future dynamic implementation through the user. For now it doesn't do anything
    public NCMPImporter(Config config) {
        super(config);
        // This is to get the datasourceIds without need to specify it in the recipe, as we already implemented DatasourceId
        datasourceIds = stringsFromEnumeration(DatasourceId.class);

    }

    protected static final Provider PROVIDER = new Provider(
            "uk.nhs.phe",
            "Public Health England"
    );

    private enum DatasourceId {
        NCMPImporter(new DatasourceSpec(
                NCMPImporter.class,
                "childhoodObesity",
                "Childhood Obesity",
                "",
                DATASOURCE)
        );
        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }


    // This is implemented once per dataset and we create a new object new Provider() with the details of our dataset
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // This is implemented once per dataset and we create a new object new DatasourceId object with the detailed specification of our dataset
    // including the local URL. We are returning a datasourceSpec object.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    // This is where the actual importing happens
    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // We are planning to use the column "Area code" in the excel file as our geometry. This will be the subject. However, this just
        // contains a, local authority in this case, code and not any polygon information. We get that by calling
        // the below object that allows us to get the geometry from OaImporter. At this point we are only creating a reference
        // which we will use afterwards
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        // We create an empty list that will keep our excell sheet values
        List<FixedValue> fixedValues = new ArrayList<FixedValue>();

        // We read the excel file locally
        FileInputStream excelFile = new FileInputStream(new File(DATASOURCE));
        Workbook workbook = new XSSFWorkbook(excelFile);
        DataFormatter dataFormatter = new DataFormatter();

        int sheet = 3;

        Sheet datatypeSheet = workbook.getSheetAt(sheet);

        Iterator<Row> rowIterator = datatypeSheet.rowIterator();

        // Dataset specific: this is to skip the first two lines that don't have any values of interest
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();

        while (rowIterator.hasNext()){
            Row row = rowIterator.next();

            // fetching the subject geometry from OaImporter to save it in getFixedValueAttributes
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCell(0)).trim());

            // Dataset specific: The dataset contains mixed geometries. Check that the geometries in the excel file
            // match the "Area code" column. If they are not null proceed
            if (subject!=null){

                // Dataset specific: attributeIndex is the column index that we are interested in. In this case the proportion
                // of children that have found to be with excess weight
                int attributeIndex = 2;

                for (Attribute attribute : datasource.getFixedValueAttributes()) {
                    fixedValues.add(new FixedValue(
                            subject,
                            attribute,
                            dataFormatter.formatCellValue(row.getCell(attributeIndex))));

                    // Dataset specific: attributeIndex needs to be increment by 5 as this is the next column of interest
                    attributeIndex+=5;
                }
            }
        }

//        saveAndClearFixedValueBuffer(fixedValues);

        FixedValueUtils.save(fixedValues);
        fixedValues.clear();

    }
    @Override
    public List<Attribute> getFixedValueAttributes(String datasourceID) {

        List<Attribute> attributes = new ArrayList<>();
        // Dataset specific: we hardcode the columns from the excel sheet
        String[] elements = { "reception_excess_2010_2012", "reception_excess_2011_2013", "reception_excess_2012_2014"};

        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }

}
