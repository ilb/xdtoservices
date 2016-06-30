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
package ru.ilb.xdtoservices.core;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author slavb
 */
@Component
public class XmlMergeImpl {

    public String mergeXml(String strBase, String strPatch) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();

            InputSource isBase = new InputSource();
            isBase.setCharacterStream(new StringReader(strBase));
            Document docBase = db.parse(isBase);

            NodeList nodesBase = docBase.getDocumentElement().getChildNodes();

            InputSource isPatch = new InputSource();
            isPatch.setCharacterStream(new StringReader(strPatch));
            Document docPatch = db.parse(isPatch);

            NodeList nodesPatch = docPatch.getDocumentElement().getChildNodes();
            //NodeList nodesPatch = docPatch.getDocumentElement().getElementsByTagName("*");
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();

            Map<String, Element> elementsBase = new HashMap<>();
            Set<Element> elementsForRemove = new HashSet<>();

            for (int i = 0; i < nodesBase.getLength(); i++) {
                if (nodesBase.item(i) instanceof Element) {
                    Element element = (Element) nodesBase.item(i);
                    if (!elementsBase.containsKey(element.getNodeName())) {
                        elementsBase.put(element.getNodeName(), element);
                    }
                }
            }
            for (int i = 0; i < nodesPatch.getLength(); i++) {
                if (nodesPatch.item(i) instanceof Element) {
                    Element elementPatch = (Element) nodesPatch.item(i);
                    if (!hasElementNodes(elementPatch.getChildNodes())) {
                        // не табличная часть
                        //System.out.println(element.getNodeName() + " = " + element.getTextContent());
                        if (!elementsBase.containsKey(elementPatch.getNodeName())) {
                            throw new IllegalArgumentException("Неизвестный элемент " + elementPatch.getNodeName()+ "\nstrBase="+strBase + " \nstrPatch="+strPatch);
                        }
                        // patch -> base
                        Element el=elementsBase.get(elementPatch.getNodeName());
                        Node nodeCopy = docBase.importNode(elementPatch, true);
                        el.getParentNode().insertBefore(nodeCopy, el);
                        el.getParentNode().removeChild(el);
                        //elementsBase.get(elementPatch.getNodeName()).setTextContent(elementPatch.getTextContent());

                        //Node book = (Node) xpath.evaluate("//book[author='Neal Stephenson']", docBase, XPathConstants.NODE);                        // не табличная часть, найдем такой узел в оригинале
                        //NodeList nodesBase = docBase.getDocumentElement().getElementsByTagName(strBase);
                    } else { //табличная часть, заменим целиком 
                        Element firstElBase = null;
                        if (elementsBase.containsKey(elementPatch.getNodeName())) {
                            firstElBase = elementsBase.get(elementPatch.getNodeName());
                            //все пометим для удаления
                            for (Element el = firstElBase; el!=null && el.getNodeName().equals(elementPatch.getNodeName()); el = getNextElementSibling(el)) {
                                elementsForRemove.add(el);
                            }
                        }
                        Node nodeCopy = docBase.importNode(elementPatch, true);
                        if (firstElBase != null) {
                            firstElBase.getParentNode().insertBefore(nodeCopy, firstElBase);
                        } else {
                            docBase.getDocumentElement().appendChild(nodeCopy);
                        }
                    }
                }
            }
            //удалим замененные узлы
            for (Element el:elementsForRemove){
                el.getParentNode().removeChild(el);
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult resultStream = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(docBase);
            transformer.transform(source, resultStream);
            String result = resultStream.getWriter().toString();
            return result;
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean hasElementNodes(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                return true;
            }
        }
        return false;
    }

    private Element getNextElementSibling(Element el) {
        Node childNode = el;
        while (childNode.getNextSibling() != null) {
            childNode = childNode.getNextSibling();
            if (childNode instanceof Element) {
                return (Element) childNode;
            }
        }
        return null;
    }

    private Element getPreviousElementSibling(Element el) {
        Node childNode = el;
        while (childNode.getPreviousSibling() != null) {
            childNode = childNode.getPreviousSibling();
            if (childNode instanceof Element) {
                return (Element) childNode;
            }
        }
        return null;
    }

}
