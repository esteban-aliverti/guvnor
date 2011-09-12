/*
 * Copyright 2011 JBoss by Red Hat.
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
package org.drools.guvnor.client.ruleeditor.changeset;

/**
 * The creation of a new <resource> element (unfortunately) is not always 
 * synchronous (see {@link CreateAssetResourceWidget#getResourceElement(ResourceElementReadyCommand)}
 * This command is used to be notified when the <resource> element is ready to
 * be used.  
 */
public interface ResourceElementReadyCommand {
    void onSuccess(String resource);
    void onFailure(Throwable cause);
}
