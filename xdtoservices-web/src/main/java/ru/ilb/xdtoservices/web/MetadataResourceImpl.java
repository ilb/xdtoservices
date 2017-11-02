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
import com.ipc.oce.xml.oc.OCXDTOFactory;
import com.ipc.oce.xml.oc.OCXMLSchema;
import com.ipc.oce.xml.oc.OCXMLSchemaSet;
import java.io.IOException;
import javax.ws.rs.Path;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.JIAutomationException;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ilb.xdtoservices.api.MetadataResource;
import ru.ilb.xdtoservices.core.OCApplicationPool;

@Path("metadata")
public class MetadataResourceImpl implements MetadataResource {

    @Autowired
    OCApplicationPool applicationPool;

    @Override
    public String exportXMLSchema(String uriNamespace) {

        try {
            OCApp app = applicationPool.getApplication();
            OCXDTOFactory factory = app.getXDTOFactory();
            String result = "";
            if (uriNamespace == null) {
                uriNamespace = factory.getCurrentConfigURI();
            }
            OCXMLSchemaSet schemaSet = factory.exportXMLSchema(uriNamespace);
            OCXMLSchema schema = schemaSet.getSchema(0);
            result = schema.getSchemaAsString();

            //factory.exportXMLSchema(uriNamespaces);
            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
