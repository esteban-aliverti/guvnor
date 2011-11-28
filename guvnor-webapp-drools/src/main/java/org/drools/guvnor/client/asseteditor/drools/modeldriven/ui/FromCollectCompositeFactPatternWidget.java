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

import java.util.HashMap;
import java.util.Map;

import org.drools.guvnor.client.asseteditor.drools.modeldriven.HumanReadable;
import org.drools.guvnor.client.common.ClickableLabel;
import org.drools.guvnor.client.common.DirtyableFlexTable;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.brl.FreeFormLine;
import org.drools.ide.common.client.modeldriven.brl.FromAccumulateCompositeFactPattern;
import org.drools.ide.common.client.modeldriven.brl.FromCollectCompositeFactPattern;
import org.drools.ide.common.client.modeldriven.brl.FromCompositeFactPattern;
import org.drools.ide.common.client.modeldriven.brl.IPattern;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class FromCollectCompositeFactPatternWidget extends FromCompositeFactPatternWidget {

    private Map<String,String> extraLeftSidePatternFactTypes = null;

    public FromCollectCompositeFactPatternWidget(RuleModeller modeller,
            FromCollectCompositeFactPattern pattern) {
        super(modeller, pattern);
    }

    public FromCollectCompositeFactPatternWidget(RuleModeller modeller,
            FromCollectCompositeFactPattern pattern,Boolean readOnly) {
        super(modeller, pattern, readOnly);
    }

    private void initExtraLeftSidePatternFactTypes(){
        extraLeftSidePatternFactTypes = new HashMap<String, String>();
        extraLeftSidePatternFactTypes.put("Collection","java.util.Collection");
        extraLeftSidePatternFactTypes.put("List","java.util.List");
        extraLeftSidePatternFactTypes.put("Set","java.util.Set");
    }

    @Override
    protected Widget getCompositeLabel() {
        ClickHandler  leftPatternclick = new ClickHandler() {

            public void onClick(ClickEvent event) {
                Widget w = (Widget) event.getSource();
                showFactTypeSelector(w);

            }
        };
        ClickHandler  rightPatternclick = new ClickHandler() {

            public void onClick(ClickEvent event) {
                Widget w = (Widget) event.getSource();
                showRightPatternSelector(w);
            }
        };


        String lbl = "<div class='form-field'>" + HumanReadable.getCEDisplayName("from collect") + "</div>";

        DirtyableFlexTable panel = new DirtyableFlexTable();

        int r = 0;

        if (pattern.getFactPattern() == null) {
            panel.setWidget(r++, 0, new ClickableLabel("<br> <font color='red'>" + constants.clickToAddPatterns() + "</font>", leftPatternclick, !this.readOnly));
        }


        panel.setWidget(r++, 0, new HTML(lbl));

        if (this.getFromCollectPattern().getRightPattern() == null) {
            panel.setWidget(r++, 0, new ClickableLabel("<br> <font color='red'>" + constants.clickToAddPatterns() + "</font>", rightPatternclick, !this.readOnly));
        } else {
            IPattern rPattern = this.getFromCollectPattern().getRightPattern();

            RuleModellerWidget patternWidget = null;
            if (rPattern instanceof FactPattern) {
                patternWidget = new FactPatternWidget(this.getModeller(), rPattern, true, true, this.readOnly);
            } else if (rPattern instanceof FromAccumulateCompositeFactPattern) {
                patternWidget = new FromAccumulateCompositeFactPatternWidget(this.getModeller(), (FromAccumulateCompositeFactPattern) rPattern,this.readOnly);
            } else if (rPattern instanceof FromCollectCompositeFactPattern) {
                patternWidget = new FromCollectCompositeFactPatternWidget(this.getModeller(), (FromCollectCompositeFactPattern) rPattern,this.readOnly);
            } else if (rPattern instanceof FromCompositeFactPattern) {
                patternWidget = new FromCompositeFactPatternWidget(this.getModeller(), (FromCompositeFactPattern) rPattern,this.readOnly);
            } else if (rPattern instanceof FreeFormLine) {
                patternWidget = new FreeFormLineWidget(this.getModeller(), (FreeFormLine) rPattern,this.readOnly);
            } else {
                throw new IllegalArgumentException("Unsuported pattern " + rPattern + " for right side of FROM COLLECT");
            }

            patternWidget.addOnModifiedCommand(new Command() {
                public void execute() {
                    setModified(true);
                }
            });

            panel.setWidget(r++, 0,
                    addRemoveButton(patternWidget, new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            if (Window.confirm(constants
                                    .RemoveThisBlockOfData())) {
                                setModified(true);
                                getFromCollectPattern().setRightPattern(null);
                                getModeller().refreshWidget();
                            }
                        }
                    }));
        }

        return panel;
    }

    @Override
    protected void showFactTypeSelector(final Widget w) {

        final FormStylePopup popup = new FormStylePopup();
        popup.setTitle(constants.NewFactPattern());

        final ListBox box = new ListBox();

        box.addItem(constants.Choose());

        for (Map.Entry<String, String> entry : this.getExtraLeftSidePatternFactTypes().entrySet()) {
            box.addItem(entry.getKey(), entry.getValue());
        }
        
        //TODO: Add Facts that extedns Collection
//        box.addItem("...");
//        box.addItem("TODO: Add Facts that extedns Collection");

        box.setSelectedIndex(0);
        box.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                pattern.setFactPattern(new FactPattern(box.getValue(box
                        .getSelectedIndex())));
                setModified(true);
                getModeller().refreshWidget();
                popup.hide();
            }
        });


        popup.addAttribute(constants.chooseFactType(),
                box);

        popup.show();
    }

    /**
     * Pops up the fact selector.
     */
    protected void showRightPatternSelector(final Widget w) {
        final ListBox box = new ListBox();
        SuggestionCompletionEngine completions = this.getModeller().getSuggestionCompletions();
        String[] facts = completions.getFactTypes();

        box.addItem(constants.Choose());
        for (int i = 0; i < facts.length; i++) {
            box.addItem(facts[i]);
        }
        box.setSelectedIndex(0);

        final FormStylePopup popup = new FormStylePopup();
        popup.setTitle(constants.NewFactPattern());
        popup.addAttribute(constants.chooseFactType(),
                box);
        box.addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                getFromCollectPattern()
                        .setRightPattern(
                                new FactPattern(box.getItemText(box
                                        .getSelectedIndex())));
                setModified(true);
                getModeller().refreshWidget();
                popup.hide();

            }
        });

        final Button freeFormDRLBtn = new Button(constants.FreeFormDrl());
        final Button fromBtn = new Button(constants.From());
        final Button fromAccumulateBtn = new Button(constants.FromAccumulate());
        final Button fromCollectBtn = new Button(constants.FromCollect());

        ClickHandler btnsClickHandler = new ClickHandler() {

            public void onClick(ClickEvent event) {
                Widget sender = (Widget)event.getSource();
                if (sender == fromBtn) {
                    getFromCollectPattern().setRightPattern(new FromCompositeFactPattern());
                } else if (sender == fromAccumulateBtn) {
                    getFromCollectPattern().setRightPattern(new FromAccumulateCompositeFactPattern());
                } else if (sender == fromCollectBtn) {
                    getFromCollectPattern().setRightPattern(new FromCollectCompositeFactPattern());
                } else if (sender == freeFormDRLBtn) {
                    getFromCollectPattern().setRightPattern(new FreeFormLine());
                } else {
                    throw new IllegalArgumentException("Unknown sender: " + sender);
                }
                setModified(true);
                getModeller().refreshWidget();
                popup.hide();

            }
        };
        

        freeFormDRLBtn.addClickHandler(btnsClickHandler);
        fromBtn.addClickHandler(btnsClickHandler);
        fromAccumulateBtn.addClickHandler(btnsClickHandler);
        fromCollectBtn.addClickHandler(btnsClickHandler);

        popup.addAttribute("", freeFormDRLBtn);
        popup.addAttribute("", fromBtn);
        popup.addAttribute("", fromAccumulateBtn);
        popup.addAttribute("", fromCollectBtn);


        popup.show();
    }

    private FromCollectCompositeFactPattern getFromCollectPattern() {
        return (FromCollectCompositeFactPattern) this.pattern;
    }

    @Override
    protected void calculateReadOnly() {
        if (this.pattern.factPattern != null) {
            this.readOnly = !(this.getExtraLeftSidePatternFactTypes().containsKey(this.pattern.factPattern.getFactType()) 
                    || this.getModeller().getSuggestionCompletions().containsFactType(this.pattern.factPattern.getFactType()));
        }
    }

    private Map<String,String> getExtraLeftSidePatternFactTypes(){
        if (this.extraLeftSidePatternFactTypes == null){
            this.initExtraLeftSidePatternFactTypes();
        }
        return this.extraLeftSidePatternFactTypes;
    }
}
