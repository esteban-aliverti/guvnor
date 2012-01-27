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

package org.drools.ide.common.client.factconstraints.customform;

import java.io.Serializable;

public interface CustomFormConfiguration extends Serializable{

    boolean isUseFormIdForRule();
    void setUseFormIdForRule(boolean useFormIdForRule);
    
    String getFactType();
    void setFactType(String factType);

    String getFieldName();
    void setFieldName(String fieldName);

    String getCustomFormURL();
    void setCustomFormURL(String url);

    int getCustomFormHeight();
    void setCustomFormHeight(int height);

    int getCustomFormWidth();
    void setCustomFormWidth(int width);

}
