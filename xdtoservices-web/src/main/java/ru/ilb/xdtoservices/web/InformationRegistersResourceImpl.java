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
import com.ipc.oce.OCStructure;
import com.ipc.oce.exceptions.ConfigurationException;
import com.ipc.oce.objects.OCInformationRegisterCollection;
import com.ipc.oce.objects.OCInformationRegisterManager;
import com.ipc.oce.objects.OCInformationRegisterRecordSet;
import com.ipc.oce.xml.oc.OCXDTOSerializer;
import com.ipc.oce.xml.oc.OCXMLReader;
import java.io.IOException;
import javax.ws.rs.Path;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.JIAutomationException;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ilb.xdtoservices.api.InformationRegistersResource;
import ru.ilb.xdtoservices.core.OCApplicationPool;

@Path("informationregisters")
public class InformationRegistersResourceImpl implements InformationRegistersResource {
    @Autowired
    OCApplicationPool applicationPool;
    
    @Override
    public String list(String registerName) {
        try {
            OCApp app = applicationPool.getApplication();
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            OCInformationRegisterCollection collection = app.getInformationRegisterCollection();
            OCInformationRegisterManager regManager = collection.getInformationRegister(registerName);
            OCStructure filter=regManager.createEmptyStruct();
//            OCCatalogManager contrManager = app.getCatalogManager("ФизическиеЛица");
//            OCCatalogRef cont=contrManager.getRef(app.createUUID("d7f8f042-33a9-11e6-a9e1-08002711c175"));
//            filter.insert("Физлицо", cont);
//            OCInformationRegisterSelection selection = regManager.select(null, null, filter, null);
//            int stopFactor = 2;
//            while ((stopFactor--) != 0 && selection.next()) {
//                System.out.println("stop factor: " + stopFactor);
//                System.out.println("\tLineNumber: " + selection.getLineNumber());
//                System.out.println("\tВидДокумента: " + selection.getDimension("ВидДокумента"));
//                
//                System.out.println("\tСерия: " + selection.getResource("Серия"));
//                System.out.println("\tНомер: " + selection.getResource("Номер"));
//                
//                System.out.println("\tRecorder: " + selection.getRecorder());
//            }   
//            //System.out.println("Selection size: " + selection.size()); // never call it
//            System.out.println("===============================================\n");
//            OCInformationRegisterRecordSet rs=regManager.createRecordSet();
//            OCFilter f=rs.getFilter();
//            f.getItem("Физлицо").set(new OCVariant(cont),true);
            //f.getItem("Физлицо").setComparisonType(new EComparisonType(EComparisonType.EQUALS));
            
            
            String xml1 = "<InformationRegisterRecordSet.ДокументыФизическихЛиц xmlns=\"http://v8.1c.ru/8.1/data/enterprise/current-config\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
"	<Filter>\n" +
"		<FilterItem xmlns=\"http://v8.1c.ru/8.1/data/enterprise\">\n" +
//"			<Name xsi:type=\"xs:string\">Физлицо</Name>\n" +

"			<Name>Физлицо</Name>\n" +
//"			<Value>d7f8f042-33a9-11e6-a9e1-08002711c175</Value>\n" +
"			<Value xmlns:d4p1=\"http://v8.1c.ru/8.1/data/enterprise/current-config\" xsi:type=\"d4p1:CatalogRef.ФизическиеЛица\">d7f8f042-33a9-11e6-a9e1-08002711c175</Value>\n" +"		</FilterItem>\n" +
"	</Filter></InformationRegisterRecordSet.ДокументыФизическихЛиц>";
            
String xml2            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:InformationRegisterRecordSet.ДокументыФизическихЛиц xmlns:ns0=\"http://v8.1c.ru/8.1/data/enterprise/current-config\" xmlns:ns2=\"http://v8.1c.ru/8.1/data/enterprise\" xmlns:ns1=\"http://v8.1c.ru/8.1/data/core\"><ns0:Filter><ns2:FilterItem><ns2:Name>Физлицо</ns2:Name><ns2:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns0:CatalogRef.ФизическиеЛица\">d7f8f042-33a9-11e6-a9e1-08002711c175</ns2:Value></ns2:FilterItem></ns0:Filter></ns0:InformationRegisterRecordSet.ДокументыФизическихЛиц>";
            //rs.getRecord(0).setAttribute("Номер", new OCVariant("444222"));
            //rs.write(true);
            
            OCXMLReader reader = app.newXMLReader();
            reader.setString(xml2);
            
            OCObject object = serializer.readXML(reader);
            OCInformationRegisterRecordSet commonObject = new OCInformationRegisterRecordSet(object);            
            commonObject.read();
            String res = serializer.writeXML(commonObject);
            
            return res;

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }    
    }

    @Override
    public void create(String string) {
        try {
            OCApp app = applicationPool.getApplication();
            OCXDTOSerializer serializer = app.getXDTOSerializer();
            
            OCXMLReader reader = app.newXMLReader();
            reader.setString(string);
            
            OCObject object = serializer.readXML(reader);
            OCInformationRegisterRecordSet commonObject = new OCInformationRegisterRecordSet(object);            
            commonObject.write();
            //String res = serializer.writeXML(commonObject);

        } catch (JIAutomationException ex) {
            throw new RuntimeException(ex.getExcepInfo().getExcepDesc());
        } catch (JIException | IOException | ConfigurationException ex) {
            throw new RuntimeException(ex);
        }    
    }
    
}
