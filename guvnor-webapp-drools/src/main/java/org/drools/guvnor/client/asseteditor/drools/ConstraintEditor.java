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

package org.drools.guvnor.client.asseteditor.drools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.ide.common.client.factconstraints.ConstraintConfiguration;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ConstraintEditor extends Composite {
    private ConstraintConfiguration config;

    public ConstraintEditor(ConstraintConfiguration config) {
        this.config = config;

        Grid confGrid = new Grid( config.getArgumentKeys().size(),
                                  2 );

        ArrayList<String> list = new ArrayList<String>();
        Map<String, String> argI18N = new HashMap<String, String>();
        for ( String arg : config.getArgumentKeys() ) {
            String i18n = getI18NText( arg );
            list.add( i18n );
            argI18N.put( i18n,
                         arg );
        }
        Collections.sort( list );

        int row = 0;
        for ( String arg : list ) {
            TextBox argTB = new TextBox();
            argTB.setText( getConstraintConfiguration().getArgumentValue( arg ).toString() );
            argTB.setName( argI18N.get( arg ) );
            argTB.setTitle( arg );
            argTB.addChangeListener( new ChangeListener() {
                public void onChange(Widget sender) {
                    TextBox argTB = (TextBox) sender;
                    getConstraintConfiguration().setArgumentValue( argTB.getName(),
                                                                   argTB.getText() );
                }
            } );

            confGrid.setWidget( row,
                                0,
                                new Label( arg + ":" ) );
            confGrid.setWidget( row,
                                1,
                                argTB );
            row++;
        }

        initWidget( confGrid );
    }

    private String getI18NText(String s) {
        // TODO use a switch to return the correct i18n text from the messages
        return s;
    }

    public ConstraintConfiguration getConstraintConfiguration() {
        return config;
    }

    public void setConstraintConfiguration(ConstraintConfiguration config) {
        this.config = config;
    }

    public String getConstraintName() {
        return getConstraintConfiguration().getConstraintName();
    }
}
