/*
 * Copyright 2011 JBoss Inc
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

package org.drools.guvnor.client.widgets.wizards.assets.decisiontable;

import java.util.List;

import org.drools.guvnor.client.decisiontable.DTCellValueWidgetFactory;
import org.drools.ide.common.client.modeldriven.dt52.ConditionCol52;
import org.drools.ide.common.client.modeldriven.dt52.GuidedDecisionTable52.TableFormat;
import org.drools.ide.common.client.modeldriven.dt52.Pattern52;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View and Presenter definitions for the Fact Pattern Constraints page
 */
public interface FactPatternConstraintsPageView
        extends
        IsWidget {

    interface Presenter {

        void selectPattern(Pattern52 pattern);

        void setChosenConditions(Pattern52 pattern,
                                 List<ConditionCol52> conditions);

        String[] getOperatorCompletions(Pattern52 selectedPattern,
                                        ConditionCol52 selectedCondition);

        void stateChanged();

        TableFormat getTableFormat();

    }

    /**
     * Set the Presenter for the View to callback to
     * 
     * @param presenter
     */
    void setPresenter(Presenter presenter);

    void setDTCellValueWidgetFactory(DTCellValueWidgetFactory factory);

    void setAvailablePatterns(List<Pattern52> patterns);

    void setAvailableFields(List<AvailableField> fields);

    void setChosenConditions(List<ConditionCol52> conditions);

    void setArePatternBindingsUnique(boolean arePatternBindingsUnique);

    void setAreFactPatternsDefined(boolean areFactPatternsDefined);

    void setAreConditionsDefined(boolean areConditionsDefined);

}
