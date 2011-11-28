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

package org.drools.guvnor.client.asseteditor.drools.modeldriven.ui;

import java.util.ArrayList;
import java.util.List;

import org.drools.guvnor.client.common.DirtyableComposite;

import com.google.gwt.user.client.Command;

/**
 * A superclass for the widgets present in RuleModeller. 
 */
public abstract class RuleModellerWidget extends DirtyableComposite {

    private RuleModeller modeller;

    private boolean modified;

    private List<Command> onModifiedCommands = new ArrayList<Command>();

    public RuleModellerWidget(RuleModeller modeller) {
        this.modeller = modeller;
    }

    /**
     * Dictates if the widget's state is RO or not. Sometimes RuleModeller will
     * force this state (i.e. when lockLHS() or lockRHS()), but some other times,
     * the widget itself is responsible to autodetect its state.
     * @return
     */
    public abstract boolean isReadOnly();

    public RuleModeller getModeller() {
        return modeller;
    }

    protected void setModified(boolean modified) {
        if (modified){
            executeOnModifiedCommands();
        }
        this.modified = modified;
    }
    
    protected boolean isModified() {
        return modified;
    }

    public void addOnModifiedCommand(Command command){
        this.onModifiedCommands.add(command);
    }

    private void executeOnModifiedCommands(){
        for (Command command : onModifiedCommands) {
            command.execute();
        }
    }

}
