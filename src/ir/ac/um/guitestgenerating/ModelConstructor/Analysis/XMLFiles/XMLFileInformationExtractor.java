package ir.ac.um.guitestgenerating.ModelConstructor.Analysis.XMLFiles;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLFileInformationExtractor {
    private String xmlFile;

    public XMLFileInformationExtractor(String xmlFilePath){
        this.xmlFile = xmlFilePath;
    }

    public void setXmlFile (String xmlFile){
        this.xmlFile = xmlFile;
    }


    public  String findViewLabelById(String viewId) {
        return null;
    }
    public Document getXMLDocumentStructure(String filePath){
        Document doc = null;
        try{
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

        } catch (Exception e) {
           e.printStackTrace();
        }
        return doc;

    }

    public String extractAttributeValue(Element eElement, String attribute) {
        String attributeValue = eElement.getAttribute(attribute);
        if (!attributeValue.isEmpty())
            attributeValue.trim();
        return attributeValue;
    }

    public String getXmlFilePath(){
        return this.xmlFile;
    }

}
