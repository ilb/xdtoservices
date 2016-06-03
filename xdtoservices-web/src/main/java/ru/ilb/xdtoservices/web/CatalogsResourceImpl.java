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
import com.ipc.oce.OCObject;
import com.ipc.oce.exceptions.ConfigurationException;
import com.ipc.oce.objects.OCCatalogManager;
import com.ipc.oce.objects.OCCatalogObject;
import com.ipc.oce.objects.OCCatalogSelection;
import com.ipc.oce.objects._OCCommonObject;
import com.ipc.oce.xml.oc.OCXDTOSerializer;
import com.ipc.oce.xml.oc.OCXMLReader;
import com.ipc.oce.xml.oc.OCXMLWriter;
import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.Path;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.JIAutomationException;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ilb.xdtoservices.api.CatalogsResource;
import ru.ilb.xdtoservices.core.OCApplicationPool;

@Path("catalogs")
public class CatalogsResourceImpl implements CatalogsResource {

    public static final String SYS_NS = "urn:ru:ilb:xdtoservices:xdtoservices";

    @Autowired
    OCApplicationPool ocApplicationPool;

    @Override
    public String getCatalog(String catalogName) {

        OCCatalogManager manager;
        StringBuffer sb = new StringBuffer(4096);
        try {
            OCApp app = ocApplicationPool.getApplication();
            manager = app.getCatalogManager(catalogName);
            OCCatalogSelection selection = manager.select();

            OCXDTOSerializer serializer = app.getXDTOSerializer();
            while (selection.next()) {
                OCCatalogObject object = selection.getObject();
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
        sb.insert(0, "<xdto:catalog xmlns:xdto=\"" + SYS_NS + "\">");
        sb.append("</xdto:catalog>");
    }

    @Override
    public String getCatalogObject(String catalogName, UUID uid) {

        try {
            OCApp app = ocApplicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogObject catalogObject = catalogManager.getRef(app.createUUID(uid.toString())).getObject();
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLWriter writer = app.newXMLWriter();
            writer.setString("UTF-8");

            serializer.writeXML(writer, catalogObject);
            return writer.close();

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public UUID createCatalogObject(String catalogName, String string) {
        try {
            OCApp app = ocApplicationPool.getApplication();
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLReader reader = app.newXMLReader();
            reader.setString(string);
            OCObject object = serializer.readXML(reader);
            _OCCommonObject commonObject = new _OCCommonObject(object);
            commonObject.write();
            return UUID.fromString(commonObject.getRef().getUUID().toString());

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
