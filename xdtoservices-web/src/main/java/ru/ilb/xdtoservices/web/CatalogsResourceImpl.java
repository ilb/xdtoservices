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
import com.ipc.oce.OCVariant;
import com.ipc.oce.exceptions.ConfigurationException;
import com.ipc.oce.objects.OCCatalogManager;
import com.ipc.oce.objects.OCCatalogObject;
import com.ipc.oce.objects.OCCatalogRef;
import com.ipc.oce.objects.OCCatalogSelection;
import com.ipc.oce.objects.OCUUID;
import com.ipc.oce.objects._OCCommonObject;
import com.ipc.oce.objects._OCCommonRef;
import com.ipc.oce.xml.oc.OCXDTOSerializer;
import com.ipc.oce.xml.oc.OCXMLReader;
import com.ipc.oce.xml.oc.OCXMLWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;
import javax.ws.rs.Path;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.JIAutomationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import ru.ilb.xdtoservices.api.CatalogsResource;
import ru.ilb.xdtoservices.core.OCApplicationPool;
import ru.ilb.xdtoservices.core.XmlMergeImpl;

@Path("catalogs")
public class CatalogsResourceImpl implements CatalogsResource {

    public static final String SYS_NS = "urn:ru:ilb:xdtoservices:xdtoservices";

    @Autowired
    OCApplicationPool applicationPool;

    @Autowired
    CatalogsResourceIntr catalogsResourceIntr;

    @Autowired
    XmlMergeImpl xmlMergeImpl;

    @Override
    public String list(String catalogName) {

        OCCatalogManager manager;
        StringBuffer sb = new StringBuffer(4096);
        try {
            OCApp app = applicationPool.getApplication();
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
        sb.insert(0, "<xdto:CatalogObjects xmlns:xdto=\"" + SYS_NS + "\">");
        sb.append("</xdto:CatalogObjects>");
    }

    @Override
    public String find(String catalogName, UUID uid) {

        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogRef catalogRef = catalogManager.getRef(app.createUUID(uid.toString()));
            String result = null;
            if (!catalogRef.toString().contains("Объект не найден")) { //FIXME
                OCCatalogObject catalogObject = catalogRef.getObject();
                OCXDTOSerializer serializer = app.getXDTOSerializer();
                OCXMLWriter writer = app.newXMLWriter();
                writer.setString("UTF-8");

                serializer.writeXML(writer, catalogObject);

                result = writer.close();
            }

            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    @Cacheable("catalogs")
    public String getTemplate(String catalogName) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogObject catalogObject = catalogManager.createItem();
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
    public UUID create(String catalogName, String string) {
        try {
            OCApp app = applicationPool.getApplication();
            String baseXml = getTemplate(catalogName);
            String patchedXml = xmlMergeImpl.mergeXml(baseXml, string);

            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLReader reader = app.newXMLReader();
            reader.setString(patchedXml);

//            OCXDTOFactory factory=app.getXDTOFactory();
//            OCXDTOObjectType type=factory.createObjectType(factory.getCurrentConfigURI(), "CatalogObject."+ catalogName);
//            OCXDTODataObject dataObject = factory.readXML(string, type);
//            //OCXDTODataObject dataObject = factory.createDataObject(type);
//
//            //OCObject object = serializer.readXML(reader);
//            OCVariant objectv = serializer.readXDTO(dataObject);
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

    @Override
    public String findByAttribute(String catalogName, String attributeName, String attributeValue, String attributeValueRef) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCVariant ocAttributeValue;
            if (attributeValueRef == null) {
                ocAttributeValue = new OCVariant(attributeValue);
            } else {
                OCUUID ocuuid = app.createUUID(attributeValue);
                _OCCommonRef ref;
                try {
                    Object manager = app.findManager(attributeValueRef); // named data object
                    Method method = manager.getClass().getMethod("getRef", OCUUID.class);
                    ref = (_OCCommonRef) method.invoke(manager, ocuuid);
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                ocAttributeValue = new OCVariant(ref);
            }

            OCCatalogRef catalogObjectRef = catalogManager.findByAttribute(attributeName, ocAttributeValue, null, null);
            String result = null;
            if (!catalogObjectRef.isEmpty()) {

                OCXDTOSerializer serializer = app.getXDTOSerializer();
                OCXMLWriter writer = app.newXMLWriter();
                writer.setString("UTF-8");

                serializer.writeXML(writer, catalogObjectRef.getObject());

                result = writer.close();
            }

            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public UUID findRefByAttribute(String catalogName, String attributeName, String attributeValue, String attributeValueRef) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCVariant ocAttributeValue;
            if (attributeValueRef == null) {
                ocAttributeValue = new OCVariant(attributeValue);
            } else {
                OCUUID ocuuid = app.createUUID(attributeValue);
                _OCCommonRef ref;
                try {
                    Object manager = app.findManager(attributeValueRef); // named data object
                    Method method = manager.getClass().getMethod("getRef", OCUUID.class);
                    ref = (_OCCommonRef) method.invoke(manager, ocuuid);
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                ocAttributeValue = new OCVariant(ref);
            }

            OCCatalogRef catalogObjectRef = catalogManager.findByAttribute(attributeName, ocAttributeValue, null, null);
            UUID result = null;
            if (!catalogObjectRef.isEmpty()) {
                result = UUID.fromString(catalogObjectRef.getUUID().toString());
            }
            return result;
        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void edit(String catalogName, UUID uid, String string) {
        try {
            OCApp app = applicationPool.getApplication();
            String baseXml = find(catalogName, uid);
            if (baseXml == null) {
                baseXml = getTemplate(catalogName);
            }
            String patchedXml = xmlMergeImpl.mergeXml(baseXml, string);

            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCXMLReader reader = app.newXMLReader();
            reader.setString(patchedXml);

            OCObject object = serializer.readXML(reader);
            _OCCommonObject commonObject = new _OCCommonObject(object);
            commonObject.write();

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String findByDescription(String catalogName, String description) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogRef catalogObjectRef = catalogManager.findByDescription(description);;
            String result = null;
            if (!catalogObjectRef.isEmpty()) {

                OCXDTOSerializer serializer = app.getXDTOSerializer();
                OCXMLWriter writer = app.newXMLWriter();
                writer.setString("UTF-8");

                serializer.writeXML(writer, catalogObjectRef.getObject());

                result = writer.close();
            }

            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public UUID findRefByDescription(String catalogName, String description) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogRef catalogObjectRef = catalogManager.findByDescription(description);;
            UUID result = null;
            if (!catalogObjectRef.isEmpty()) {
                result = UUID.fromString(catalogObjectRef.getUUID().toString());
            }

            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String findByCode(String catalogName, String code) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogRef catalogObjectRef = catalogManager.findByCode(code);;
            String result = null;
            if (!catalogObjectRef.isEmpty()) {

                OCXDTOSerializer serializer = app.getXDTOSerializer();
                OCXMLWriter writer = app.newXMLWriter();
                writer.setString("UTF-8");

                serializer.writeXML(writer, catalogObjectRef.getObject());

                result = writer.close();
            }

            return result;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void remove(String catalogName, UUID uid) {
        try {
            OCApp app = applicationPool.getApplication();
            OCCatalogManager catalogManager = app.getCatalogManager(catalogName);
            OCCatalogRef catalogRef = catalogManager.getRef(app.createUUID(uid.toString()));
            if (!catalogRef.toString().contains("Объект не найден")) { //FIXME
                OCCatalogObject catalogObject = catalogRef.getObject();
                catalogObject.delete();
            }

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
