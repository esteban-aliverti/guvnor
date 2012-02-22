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

package org.drools.guvnor.server.repository;

import java.util.Iterator;
import java.util.List;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.explorer.ExplorerNodeConfig;
import org.drools.guvnor.client.rpc.TableDataResult;
import org.drools.guvnor.client.rpc.TableDataRow;
import org.drools.repository.AssetItem;
import org.drools.repository.RulesRepository;
import org.drools.repository.UserInfo;
import org.drools.repository.UserInfo.InboxEntry;

/**
 * This manages the users "inbox".
 */
public class UserInbox {

    static final int            MAX_RECENT_EDITED = 200;

    private static final String INBOX             = "inbox";

    private final UserInfo      userInfo;

    /**
     * Create an inbox for the given user name (id)
     */
    public UserInbox(RulesRepository repo,
                     String userName) {
        this.userInfo = new UserInfo( repo,
                                      userName );
        this.userInfo.save();
    }

    /**
     * Create an inbox for the current sessions user id.
     */
    public UserInbox(RulesRepository repo) {
        this.userInfo = new UserInfo( repo );
        userInfo.save();
    }

    /**
     * This should be called when the user edits or comments on an asset. Simply
     * adds to the list...
     */
    public void addToRecentEdited(String assetId,
                                  String note) {
        addToInbox( ExplorerNodeConfig.RECENT_EDITED_ID,
                    assetId,
                    note,
                    "self" );
    }

    public void addToRecentOpened(String assetId,
                                  String note) {
        addToInbox( ExplorerNodeConfig.RECENT_VIEWED_ID,
                    assetId,
                    note,
                    "self" );
    }

    public void addToIncoming(String assetId,
                              String note,
                              String userFrom) {
        addToInbox( ExplorerNodeConfig.INCOMING_ID,
                    assetId,
                    note,
                    userFrom );
    }

    private void addToInbox(String boxName,
                            String assetId,
                            String note,
                            String userFrom) {
        assert boxName.equals( ExplorerNodeConfig.RECENT_EDITED_ID ) || boxName.equals( ExplorerNodeConfig.RECENT_VIEWED_ID ) || boxName.equals( ExplorerNodeConfig.INCOMING_ID );
        List<InboxEntry> entries = removeAnyExisting( assetId,
                                                      userInfo.readEntries( INBOX,
                                                                            boxName ) );

        if ( entries.size() >= MAX_RECENT_EDITED ) {
            entries.remove( 0 );
            entries.add( new InboxEntry( assetId,
                                         note,
                                         userFrom ) );
        } else {
            entries.add( new InboxEntry( assetId,
                                         note,
                                         userFrom ) );
        }

        userInfo.writeEntries( INBOX,
                               boxName,
                               entries );
        userInfo.save();
    }

    private List<InboxEntry> removeAnyExisting(String assetId,
                                               List<InboxEntry> inboxEntries) {
        Iterator<InboxEntry> it = inboxEntries.iterator();
        while ( it.hasNext() ) {
            InboxEntry e = it.next();
            if ( e.assetUUID.equals( assetId ) ) {
                it.remove();
                return inboxEntries;
            }
        }
        return inboxEntries;
    }

    public List<InboxEntry> loadEntries(final String inboxName) {
        List<InboxEntry> entries;
        if ( inboxName.equals( ExplorerNodeConfig.RECENT_VIEWED_ID ) ) {
            entries = loadRecentOpened();
        } else if ( inboxName.equals( ExplorerNodeConfig.RECENT_EDITED_ID ) ) {
            entries = loadRecentEdited();
        } else {
            entries = loadIncoming();

        }
        return entries;
    }

    public List<InboxEntry> loadRecentEdited() {
        return userInfo.readEntries( INBOX,
                                     ExplorerNodeConfig.RECENT_EDITED_ID );
    }

    public List<InboxEntry> loadRecentOpened() {
        return userInfo.readEntries( INBOX,
                                     ExplorerNodeConfig.RECENT_VIEWED_ID );
    }

    public List<InboxEntry> loadIncoming() {
        return userInfo.readEntries( INBOX,
                                     ExplorerNodeConfig.INCOMING_ID );
    }

    /**
     * Wipe them out, all of them.
     */
    public void clearAll() {
        userInfo.clear( INBOX,
                        ExplorerNodeConfig.RECENT_EDITED_ID );
        userInfo.clear( INBOX,
                        ExplorerNodeConfig.RECENT_VIEWED_ID );
        userInfo.clear( INBOX,
                        ExplorerNodeConfig.INCOMING_ID );
        userInfo.save();
    }

    public void clearIncoming() {
        userInfo.clear( INBOX,
                        ExplorerNodeConfig.INCOMING_ID );
        userInfo.save();
    }

    public static TableDataResult toTable(List<InboxEntry> entries,
                                          boolean showFrom) {
        TableDataResult res = new TableDataResult();
        res.currentPosition = 0;
        res.total = entries.size();
        res.hasNext = false;
        res.data = new TableDataRow[entries.size()];
        for ( int i = 0; i < entries.size(); i++ ) {
            TableDataRow tdr = new TableDataRow();
            InboxEntry e = entries.get( i );
            tdr.id = e.assetUUID;
            if ( !showFrom ) {
                tdr.values = new String[2];
                tdr.values[0] = e.note;
                tdr.values[1] = Long.toString( e.timestamp );
            } else {
                tdr.values = new String[3];
                tdr.values[0] = e.note;
                tdr.values[1] = Long.toString( e.timestamp );
                tdr.values[2] = e.from;
            }
            tdr.format = AssetFormats.BUSINESS_RULE;
            res.data[i] = tdr;
        }
        return res;
    }

    /**
     * Helper method to log the opening. Will remove any inbox items that have
     * the same id.
     */
    public synchronized static void recordOpeningEvent(AssetItem item) {
        UserInbox ib = new UserInbox( item.getRulesRepository() );
        ib.addToRecentOpened( item.getUUID(),
                              item.getName() );
        List<InboxEntry> unreadIncoming = ib.removeAnyExisting( item.getUUID(),
                                                                ib.loadIncoming() );
        ib.userInfo.writeEntries( INBOX,
                                  ExplorerNodeConfig.INCOMING_ID,
                                  unreadIncoming );

        ib.save();
    }

    /**
     * Helper method to note the event
     */
    public synchronized static void recordUserEditEvent(AssetItem item) {
        UserInbox ib = new UserInbox( item.getRulesRepository() );
        ib.addToRecentEdited( item.getUUID(),
                              item.getName() );
        ib.save();
    }

    void save() {
        userInfo.save();
    }

}
