/*
 * Copyright 2016 slavb.
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
package ru.ilb.xdtoservices.web;

import com.ipc.oce.OCApp;
import com.ipc.oce.exceptions.ConfigurationException;
import com.ipc.oce.objects.OCCatalogManager;
import com.ipc.oce.objects.OCCatalogObject;
import com.ipc.oce.objects.OCDocumentManager;
import com.ipc.oce.objects.OCDocumentObject;
import com.ipc.oce.objects.OCDocumentRef;
import com.ipc.oce.objects.OCDocumentSelection;
import com.ipc.oce.xml.oc.OCXDTOSerializer;
import com.ipc.oce.xml.oc.OCXMLWriter;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.Path;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.JIAutomationException;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ilb.xdtoservices.api.DocumentsResource;
import ru.ilb.xdtoservices.core.OCApplicationPool;

@Path("documents")
public class DocumentsResourceImpl implements DocumentsResource {
    public static final String SYS_NS = "urn:ru:ilb:xdtoservices:xdtoservices";
    
    @Autowired
    OCApplicationPool applicationPool;

    @Override
    public String list(String documentName, Date dateStart, Date dateEnd) {
        StringBuffer sb = new StringBuffer(4096);
        try {
            OCApp app = applicationPool.getApplication();
            OCDocumentManager documentManager = app.getDocumentManager(documentName);
            OCDocumentSelection documentSelection = documentManager.select(dateStart, dateEnd);
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLWriter writer = app.newXMLWriter();
            writer.setString("UTF-8");
            
            while (documentSelection.next()) {
                OCDocumentObject object = documentSelection.getObject();
                sb.append(serializer.writeXML(object));
            }

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        surroundContainersTag(sb);
        return sb.toString();
        
    }
    protected void surroundContainersTag(StringBuffer sb) {
        sb.insert(0, "<xdto:DocumentObjects xmlns:xdto=\"" + SYS_NS + "\">");
        sb.append("</xdto:DocumentObjects>");
    }


    @Override
    public UUID create(String documentName, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String find(String documentName, UUID uid) {
        try {
            OCApp app = applicationPool.getApplication();
            OCDocumentManager documentManager = app.getDocumentManager(documentName);
            OCDocumentObject documentObject = documentManager.getRef(app.createUUID(uid.toString())).getObject();
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLWriter writer = app.newXMLWriter();
            writer.setString("UTF-8");

            serializer.writeXML(writer, documentObject);
            
            String result=writer.close();
            
            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void edit(String documentName, UUID uid, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(String documentName, UUID uid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDocumentObjectTemplate(String documentName) {
        try {
            OCApp app = applicationPool.getApplication();
            OCDocumentManager documentManager = app.getDocumentManager(documentName);
            OCDocumentObject documentObject = documentManager.createDocument();
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLWriter writer = app.newXMLWriter();
            writer.setString("UTF-8");

            serializer.writeXML(writer, documentObject);
            
            String result=writer.close();
            
            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }



    
}
