/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.guvnor.client.widgets.drools.wizards.assets.decisiontable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.view.client.SelectionModel;

/**
 * A CellList that can maintain a minimum width
 */
public class MinimumWidthCellList<T> extends CellList<T> {

    private WidthCalculator<T> sizer;

    private List< ? extends T> values = new ArrayList<T>();

    public MinimumWidthCellList(final Cell<T> cell,
                                Resources resources) {
        super( cell,
               resources );
        sizer = new WidthCalculator<T>( cell );
    }

    public void setMinimumWidth(int minWidth) {
        sizer.setMinimumWidth( minWidth );
    }

    @Override
    public void setRowData(int start,
                           List< ? extends T> values) {
        //Store the values, as renderRowValues is used for partial redraws of the whole table
        this.values = values;

        //Set our width to the maximum row width of our content
        setWidth( sizer.getMaximumElementWidth( this.values ) + "px" );
        super.setRowData( start,
                          values );
    }

    @Override
    protected void renderRowValues(SafeHtmlBuilder sb,
                                   List<T> values,
                                   int start,
                                   SelectionModel< ? super T> selectionModel) {

        //Set our width to the maximum row width of our content. This method is invoked
        //when a selection changes in the CellList and when redraw() is called. Since
        //the width of a row could change when redrawing we assert our width.
        setWidth( sizer.getMaximumElementWidth( this.values ) + "px" );
        super.renderRowValues( sb,
                               values,
                               start,
                               selectionModel );
    }

}
