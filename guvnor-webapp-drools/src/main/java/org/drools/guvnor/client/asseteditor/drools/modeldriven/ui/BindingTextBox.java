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
package org.drools.guvnor.client.asseteditor.drools.modeldriven.ui;

import com.google.gwt.regexp.shared.RegExp;

/**
 * A TextBox to handle entry of Pattern\Field bindings
 */
public class BindingTextBox extends AbstractRestrictedEntryTextBox {

    // A valid binding
    private static final RegExp VALID = RegExp.compile( "(^\\$?\\w*$)" );

    @Override
    protected boolean isValidValue(String value) {
        return VALID.test( value );
    }

}
