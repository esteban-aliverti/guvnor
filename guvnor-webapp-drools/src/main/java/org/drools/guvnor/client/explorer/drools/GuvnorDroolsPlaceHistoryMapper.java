package org.drools.guvnor.client.explorer.drools;


import org.drools.guvnor.client.explorer.AssetEditorPlace;
import org.drools.guvnor.client.explorer.FindPlace;
import org.drools.guvnor.client.explorer.GuvnorPlaceHistoryMapper;
import org.drools.guvnor.client.explorer.ModuleEditorPlace;
import org.drools.guvnor.client.explorer.MultiAssetPlace;
import org.drools.guvnor.client.explorer.navigation.admin.ManagerPlace;
import org.drools.guvnor.client.explorer.navigation.browse.CategoryPlace;
import org.drools.guvnor.client.explorer.navigation.browse.InboxPlace;
import org.drools.guvnor.client.explorer.navigation.browse.StatePlace;
import org.drools.guvnor.client.explorer.navigation.deployment.SnapshotAssetListPlace;
import org.drools.guvnor.client.explorer.navigation.deployment.SnapshotPlace;
import org.drools.guvnor.client.explorer.navigation.processes.ProcessOverviewPlace;
import org.drools.guvnor.client.explorer.navigation.qa.TestScenarioListPlace;
import org.drools.guvnor.client.explorer.navigation.qa.VerifierPlace;
import org.drools.guvnor.client.explorer.navigation.reporting.ReportTemplatesPlace;
import org.drools.guvnor.client.explorer.navigation.settings.PreferencesPlace;
import org.drools.guvnor.client.explorer.navigation.tasks.GroupTasksPlace;
import org.drools.guvnor.client.explorer.navigation.tasks.PersonalTasksPlace;
import org.drools.guvnor.client.moduleeditor.AssetViewerPlace;
import org.drools.guvnor.client.widgets.drools.wizards.assets.NewGuidedDecisionTableAssetWizardContext;
import com.google.gwt.place.shared.WithTokenizers;
import org.drools.guvnor.client.explorer.navigation.runtime.RuntimePlace;

@WithTokenizers(
        {
                FindPlace.Tokenizer.class,
                AssetEditorPlace.Tokenizer.class,
                ModuleEditorPlace.Tokenizer.class,
                AssetViewerPlace.Tokenizer.class,
                ManagerPlace.Tokenizer.class,
                RuntimePlace.Tokenizer.class,
                CategoryPlace.Tokenizer.class,
                StatePlace.Tokenizer.class,
                InboxPlace.Tokenizer.class,
                MultiAssetPlace.Tokenizer.class,
                PersonalTasksPlace.Tokenizer.class,
                GroupTasksPlace.Tokenizer.class,
                ReportTemplatesPlace.Tokenizer.class,
                PreferencesPlace.Tokenizer.class,
                ProcessOverviewPlace.Tokenizer.class,
                TestScenarioListPlace.Tokenizer.class,
                VerifierPlace.Tokenizer.class,
                SnapshotPlace.Tokenizer.class,
                SnapshotAssetListPlace.Tokenizer.class,
                NewGuidedDecisionTableAssetWizardContext.Tokenizer.class
        }
)
public interface GuvnorDroolsPlaceHistoryMapper extends GuvnorPlaceHistoryMapper {
}
