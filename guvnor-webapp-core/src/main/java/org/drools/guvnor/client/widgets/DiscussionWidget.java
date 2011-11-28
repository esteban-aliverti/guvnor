/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.configurations.UserCapabilities;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.Images;
import org.drools.guvnor.client.rpc.Artifact;
import org.drools.guvnor.client.rpc.DiscussionRecord;
import org.drools.guvnor.client.rpc.PushClient;
import org.drools.guvnor.client.rpc.PushResponse;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.ServerPushNotification;
import org.drools.guvnor.client.configurations.Capability;
import org.drools.guvnor.client.util.DecoratedDisclosurePanel;
import org.drools.guvnor.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * Does the discussion panel for artifacts.
 */
public class DiscussionWidget extends Composite {

    private static Constants       constants        = GWT.create( Constants.class );
    private static Images          images           = GWT.create( Images.class );

    private VerticalPanel          commentList      = new VerticalPanel();
    private VerticalPanel          newCommentLayout = new VerticalPanel();
    private Artifact               artifact;
    private ServerPushNotification pushNotify;
    private int                    lastCount        = 0;
    private boolean         readOnly;

    @Override
    protected void onUnload() {
        super.onUnload(); //To change body of overridden methods use File | Settings | File Templates.
        PushClient.instance().unsubscribe( pushNotify );
    }

    public DiscussionWidget(final Artifact artifact, boolean readOnly) {
        this.artifact = artifact;
        this.readOnly = readOnly;

        DecoratedDisclosurePanel discussionPanel = new DecoratedDisclosurePanel( constants.Discussion() );
        discussionPanel.setWidth( "100%" );

        commentList.setWidth( "100%" );
        VerticalPanel discussionLayout = new VerticalPanel();
        discussionLayout.setWidth( "90%" );
        discussionLayout.add( commentList );

        newCommentLayout.setWidth( "100%" );
        refreshDiscussion();
        discussionLayout.add( newCommentLayout );
        showNewCommentButton();

        discussionPanel.setContent( discussionLayout );

        pushNotify = new ServerPushNotification() {
            public void messageReceived(PushResponse response) {
                if ( "discussion".equals( response.messageType ) && artifact.getUuid().equals( response.message ) ) {
                    System.err.println( "Refreshing discussion..." );
                    refreshDiscussion();
                }
            }
        };

        PushClient.instance().subscribe( pushNotify );

        initWidget( discussionPanel );
    }

    /** Hit up the server */
    public void refreshDiscussion() {
        RepositoryServiceFactory.getAssetService().loadDiscussionForAsset( artifact.getUuid(),
                                                                      new GenericCallback<List<DiscussionRecord>>() {
                                                                          public void onSuccess(List<DiscussionRecord> result) {
                                                                              updateCommentList( result );
                                                                          }
                                                                      } );
    }

    private void updateCommentList(List<DiscussionRecord> ls) {
        if ( ls.size() == lastCount ) return; //don't want to over do it boys...
        commentList.clear();
        for ( DiscussionRecord dr : ls ) {
            appendComment( dr );
        }
        lastCount = ls.size();
    }

    private Widget appendComment(DiscussionRecord r) {
        SmallLabel hrd = new SmallLabel( constants.smallCommentBy0On1Small(r.author,
                                                        new Date( r.timestamp )) );
        hrd.addStyleName( "discussion-header" );
        commentList.add( hrd );

        String[] parts = r.note.split( "\n" );

        if ( parts.length > 0 ) {
            StringBuilder txtBuilder = new StringBuilder();
            for ( int i = 0; i < parts.length; i++ ) {
                txtBuilder.append(parts[i]);
                if ( i != parts.length - 1 ) {
                    txtBuilder.append("<br/>");
                }
            }
            HTML hth = new HTML( txtBuilder.toString() );
            hth.setStyleName( "form-field" );
            commentList.add( hth );
        } else {
            Label lbl = new Label( r.note );
            lbl.setStyleName( "form-field" );
            commentList.add( lbl );
        }

        commentList.add( new HTML( "<br/>" ) );
        return hrd;
    }

    private void showNewCommentButton() {
        newCommentLayout.clear();

        HorizontalPanel hp = new HorizontalPanel();

        Button createNewComment = new Button( constants.AddADiscussionComment() );
        createNewComment.setEnabled(!this.readOnly);
        hp.add( createNewComment );

        if ( UserCapabilities.INSTANCE.hasCapability(Capability.SHOW_ADMIN) ) {
            Button adminClearAll = new Button( constants.EraseAllComments() );
            adminClearAll.setEnabled(!readOnly);
            hp.add( adminClearAll );
            adminClearAll.addClickHandler( new ClickHandler() {
                public void onClick(ClickEvent sender) {
                    if ( Window.confirm( constants.EraseAllCommentsWarning() ) ) {
                        RepositoryServiceFactory.getAssetService().clearAllDiscussionsForAsset( artifact.getUuid(),
                                                                                           new GenericCallback<java.lang.Void>() {
                                                                                               public void onSuccess(Void v) {
                                                                                                   updateCommentList( new ArrayList<DiscussionRecord>() );
                                                                                               }
                                                                                           } );
                    }
                }
            } );
        }
        
        final String feedURL = GWT.getModuleBaseURL() + "feed/discussion?package=" + ((RuleAsset)artifact).getMetaData().getPackageName()
                + "&assetName=" + URL.encode( artifact.getName() ) + "&viewUrl=" + Util.getSelfURL();       
        Image image = new Image(images.feed());
        image.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                Window.open(feedURL, "_blank", null);

            }
        });
        hp.add(image);

        newCommentLayout.add( hp );

        newCommentLayout.setCellHorizontalAlignment( hp,
                                                     HasHorizontalAlignment.ALIGN_RIGHT );
        createNewComment.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent sender) {
                showAddNewComment();
            }
        } );
    }

    private void showAddNewComment() {
        newCommentLayout.clear();
        final TextArea comment = new TextArea();
        comment.setWidth( "100%" );
        newCommentLayout.add( comment );

        Button ok = new Button( constants.OK() );
        Button cancel = new Button( constants.Cancel() );

        ok.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent sender) {
                sendNewComment( comment.getText() );
            }
        } );

        cancel.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent sender) {
                showNewCommentButton();
            }
        } );

        HorizontalPanel hp = new HorizontalPanel();
        hp.add( ok );
        hp.add( cancel );

        newCommentLayout.add( hp );

        comment.setFocus( true );
    }

    private void sendNewComment(String text) {
        newCommentLayout.clear();
        newCommentLayout.add( new Image( images.spinner() ) );
        RepositoryServiceFactory.getAssetService().addToDiscussionForAsset( artifact.getUuid(),
                                                                       text,
                                                                       new GenericCallback<List<DiscussionRecord>>() {
                                                                           public void onSuccess(List<DiscussionRecord> result) {
                                                                               showNewCommentButton();
                                                                               updateCommentList( result );
                                                                           }
                                                                       } );
    }
}
