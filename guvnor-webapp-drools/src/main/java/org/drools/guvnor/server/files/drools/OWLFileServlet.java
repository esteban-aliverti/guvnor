/*
 * Copyright 2005 JBoss Inc
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

package org.drools.guvnor.server.files.drools;



import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.drools.guvnor.server.files.FileManagerService;
import org.drools.guvnor.server.files.RepositoryServlet;
import org.drools.guvnor.server.util.FormData;

/**
 * This is for dealing with assets that have an attachment (ie assets that are really an attachment).
 */
public class OWLFileServlet extends RepositoryServlet {

    private static final long serialVersionUID = 510l;

    @Inject
    private OWLFileManagerService owlFileManagerService;
    
    /**
     * Posting accepts content of various types -
     * may be an attachement for an asset, or perhaps a repository import to process.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException,
                                                       IOException {

        response.setContentType("text/html");
        FormData data = FileManagerService.getFormData(request);

        try {
            
            owlFileManagerService.importOWL(data.getFile().getInputStream());
            response.getWriter().write("OK");
        } catch (IllegalArgumentException e) {
            response.getWriter().write(e.getMessage());
        } catch (Exception e) {
            response.getWriter().write("Unable to process import: " + e.getMessage());
        }

    }

    /**
     */
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws ServletException,
                                                 IOException {
    }

}
