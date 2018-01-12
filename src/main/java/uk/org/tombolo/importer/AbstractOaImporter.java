package uk.org.tombolo.importer;

import uk.org.tombolo.core.utils.DatabaseJournal;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.JournalEntryUtils;
import uk.org.tombolo.recipe.SubjectRecipe;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class AbstractOaImporter extends AbstractImporter {

    public AbstractOaImporter(Config config) {
        super(config);
    }

    @Override
    public void importDatasource(@Nonnull String datasourceId, List<String> geographyScope, List<String> temporalScope,
                                 List<String> datasourceLocation, @Nonnull List<SubjectRecipe> subjectRecipes, Boolean force)
                                throws Exception {
        OaImporter oaImporter = new OaImporter(config);
        oaImporter.setDownloadUtils(downloadUtils);
        for (SubjectRecipe subjectRecipe : subjectRecipes) {
            if (!DatabaseJournal.journalHasEntry(JournalEntryUtils.getJournalEntryForDatasourceId(
                "uk.org.tombolo.importer.ons.OaImporter", subjectRecipe.getSubjectType(), geographyScope,
                    temporalScope, datasourceLocation))) {
                oaImporter.importDatasource(subjectRecipe.getSubjectType(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST, subjectRecipes, false);
            }
        }
        super.importDatasource(datasourceId, geographyScope, temporalScope, datasourceLocation, subjectRecipes, force);
    }
}
