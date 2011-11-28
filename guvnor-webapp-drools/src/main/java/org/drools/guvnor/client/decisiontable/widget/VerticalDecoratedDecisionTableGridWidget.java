/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.decisiontable.widget;

import org.drools.guvnor.client.widgets.drools.decoratedgrid.ResourcesProvider;
import org.drools.ide.common.client.modeldriven.dt52.DTColumnConfig52;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Vertical implementation of DecoratedGridWidget for the Guided Decision Table
 */
public class VerticalDecoratedDecisionTableGridWidget extends AbstractDecoratedDecisionTableGridWidget {

    public VerticalDecoratedDecisionTableGridWidget(ResourcesProvider<DTColumnConfig52> resources,
                                                    DecisionTableCellFactory cellFactory,
                                                    DecisionTableCellValueFactory cellValueFactory,
                                                    EventBus eventBus) {
        super( resources,
               cellFactory,
               cellValueFactory,
               eventBus,
               new HorizontalPanel(),
               new VerticalPanel(),
               new VerticalMergableDecisionTableGridWidget( resources,
                                                            cellValueFactory,
                                                            eventBus ),
               new VerticalDecisionTableHeaderWidget( resources,
                                                      eventBus ),
               new VerticalDecisionTableSidebarWidget( resources,
                                                                    eventBus ) );
    }

    /**
     * Return a ScrollHandler to ensure the Header and Sidebar are repositioned
     * according to the position of the scroll bars surrounding the GridWidget
     */
    @Override
    protected ScrollHandler getScrollHandler() {
        return new ScrollHandler() {

            public void onScroll(ScrollEvent event) {
                headerWidget.setScrollPosition( scrollPanel.getHorizontalScrollPosition() );
                sidebarWidget.setScrollPosition( scrollPanel.getVerticalScrollPosition() );
            }

        };
    }

}
