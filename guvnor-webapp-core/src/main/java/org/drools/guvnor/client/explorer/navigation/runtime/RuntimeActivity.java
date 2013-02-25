package org.drools.guvnor.client.explorer.navigation.runtime;

import org.drools.guvnor.client.explorer.AcceptItem;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.util.Activity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import org.drools.guvnor.client.explorer.navigation.runtime.widget.AgentManager;

public class RuntimeActivity extends Activity {

    private Constants           constants = GWT.create( Constants.class );

    private final int           id;
    private final ClientFactory clientFactory;

    public RuntimeActivity(int id,
                           ClientFactory clientFactory) {
        this.id = id;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptItem tabbedPanel,
                      EventBus eventBus) {
        openSelection( tabbedPanel,
                                     id,
                                     eventBus );
    }

    public void openSelection(final AcceptItem tabbedPanel,
                                            final int id,
                                            final EventBus eventBus) {

        switch ( id ) {
            case 0 :
                tabbedPanel.add( constants.AgentsManager(),
                                 new AgentManager(clientFactory) );
                break;
        }
    }
}
