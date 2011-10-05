/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.client.explorer;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import org.drools.guvnor.client.perspectives.ChangePerspectiveEvent;
import org.drools.guvnor.client.perspectives.PerspectiveFactory;
import org.drools.guvnor.client.perspectives.PerspectivesPanel;
import org.drools.guvnor.client.perspectives.PerspectivesPanelView;
import org.drools.guvnor.client.perspectives.author.AuthorPerspective;
import org.drools.guvnor.client.perspectives.runtime.RunTimePerspective;
import org.drools.guvnor.client.rpc.PackageServiceAsync;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PerspectivesPanelTest {

    private PerspectivesPanel perspectivesPanel;
    private PerspectivesPanelView view;
    private PerspectivesPanelView.Presenter presenter;
    private EventBusMock eventBus;

    @Before
    public void setUp() throws Exception {
        view = mock(PerspectivesPanelView.class);
        ClientFactory clientFactory = mock(ClientFactory.class);
        eventBus = spy(new EventBusMock());
        when(
                clientFactory.getPerspectivesPanelView()
        ).thenReturn(
                view
        );

        PerspectiveFactory perspectiveFactory = mock(PerspectiveFactory.class);
        when(
                clientFactory.getPerspectiveFactory()
        ).thenReturn(
                perspectiveFactory
        );
        
        when(
                perspectiveFactory.getPerspective("author")
        ).thenReturn(
                new AuthorPerspective()
                
        );
        
        when(
                perspectiveFactory.getPerspective("runtime")
        ).thenReturn(
                new RunTimePerspective()
                
        );        
        
        when(
                perspectiveFactory.getRegisteredPerspectiveTypes()
        ).thenReturn(
                new String[]{"author", "runtime"}
                
        );
        
        PackageServiceAsync packageService = mock(PackageServiceAsync.class);
        when(
                clientFactory.getPackageService()
        ).thenReturn(
                packageService
        );
        
        perspectivesPanel = new PerspectivesPanel(clientFactory, eventBus);
        presenter = getPresenter();
    }

    private PerspectivesPanelView.Presenter getPresenter() {
        return perspectivesPanel;
    }

    @Test
    public void testPresenter() throws Exception {
        verify(view).setPresenter(presenter);
    }

    @Test
    public void testSetUpDefaultPerspective() throws Exception {
        verifyPerspectiveChangedToAuthor(1);
    }

    @Test
    public void testPerspectiveListIsLoaded() throws Exception {
       verify(view).addPerspective("author", "author");
       verify(view).addPerspective("runtime", "runtime");
    }

    @Test
    public void testChangePerspectiveToAuthor() throws Exception {
        presenter.onChangePerspective("author");

        // Once because of the default and once more because of the selection
        verifyPerspectiveChangedToAuthor(2);
    }

    @Test
    public void testChangePerspectiveToRunTime() throws Exception {

        presenter.onChangePerspective("runtime");

        assertTrue(eventBus.getLatestEvent() instanceof ChangePerspectiveEvent);
        assertTrue(((ChangePerspectiveEvent) eventBus.getLatestEvent()).getPerspective() instanceof RunTimePerspective);
    }

    private void verifyPerspectiveChangedToAuthor(int times) {
        ArgumentCaptor<ChangePerspectiveEvent> changePerspectiveEventArgumentCaptor = ArgumentCaptor.forClass(ChangePerspectiveEvent.class);
        verify(eventBus, times(times)).fireEvent(changePerspectiveEventArgumentCaptor.capture());

        assertTrue(changePerspectiveEventArgumentCaptor.getValue().getPerspective() instanceof AuthorPerspective);
    }

    class EventBusMock extends EventBus {

        private List<GwtEvent<?>> events = new ArrayList<GwtEvent<?>>();

        @Override public <H extends EventHandler> HandlerRegistration addHandler(GwtEvent.Type<H> type, H handler) {
            return null;
        }

        @Override public <H extends EventHandler> HandlerRegistration addHandlerToSource(GwtEvent.Type<H> type, Object source, H handler) {
            return null;
        }

        @Override public void fireEvent(GwtEvent<?> event) {
            events.add(event);
        }

        @Override public void fireEventFromSource(GwtEvent<?> event, Object source) {
        }

        public GwtEvent<?> getLatestEvent() {
            return events.get(events.size() - 1);
        }
    }
}
