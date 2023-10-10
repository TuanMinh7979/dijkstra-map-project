package com.example.demo.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import lombok.Getter;
import lombok.Setter;

import org.w3c.dom.Node;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
public class OSMToCSV {

    private Document buildXmlDocumentReader(String path) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            InputStream istStream = new FileInputStream(path);

            InputSource is = new InputSource(new InputStreamReader(istStream, "UTF-8"));
            is.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(is);
            return doc;
        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }
    }

    public void toCSVData(String inputOsmXmlFilePath, String outputGraphDataCsvPath) {
        try {

            Document doc = buildXmlDocumentReader(inputOsmXmlFilePath);

            NodeList osmNodes = doc.getElementsByTagName("node");
            NodeList osmWays = doc.getElementsByTagName("way");

            setUpNodes(osmNodes, outputGraphDataCsvPath);
            setUpEdges(osmNodes, osmWays, outputGraphDataCsvPath);
            System.out.println("Create data csv file successfully!");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void setUpNodes(NodeList osmNodes, String outputGraphDataCsvPath) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(outputGraphDataCsvPath));
            for (int i = 0; i < osmNodes.getLength(); i++) {
                Node node = osmNodes.item(i);
                List<String> nodeList = new ArrayList<>();
                nodeList.add(String.valueOf(i));
                nodeList.add(node.getAttributes().getNamedItem("id").getTextContent());
                nodeList.add(node.getAttributes().getNamedItem("lat").getTextContent());
                nodeList.add(node.getAttributes().getNamedItem("lon").getTextContent());
                nodeList.add(" ");

                String[] array = nodeList.toArray(new String[0]);
                writer.writeNext(array);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    private void setUpEdges(NodeList osmNodes, NodeList osmWays, String outputGraphDataCsvPath) {

        HashMap<Long, Integer> nodeIdIdxMap = new HashMap<>();
        for (int i = 0; i < osmNodes.getLength(); i++) {
            Node node = osmNodes.item(i);
            nodeIdIdxMap.put(Long.valueOf(node.getAttributes().getNamedItem("id").getTextContent()), i);
        }
        try {
            CSVReader csvReader = new CSVReaderBuilder(new FileReader(outputGraphDataCsvPath)).build();
            List<String[]> csvBody = csvReader.readAll();
            CSVWriter writer = new CSVWriter(new FileWriter(outputGraphDataCsvPath), ',');

            for (int k = 0; k < osmWays.getLength(); k++) {

                Node wayi = osmWays.item(k);
                if (wayi.hasChildNodes()) {
                    NodeList wayPointNodes = wayi.getChildNodes();
                    List<Node> ndlist = new ArrayList<>();
                    for (int j = 0; j < wayPointNodes.getLength() - 1; j++) {
                        if (wayPointNodes.item(j).getNodeName().equals("nd")) {
                            ndlist.add(wayPointNodes.item(j));
                        }
                    }
                    for (int l = 0; l < (ndlist.size() - 1); l++) {

                        Node nd1 = ndlist.get(l);
                        Node nd2 = ndlist.get(l + 1);

                        int nd1MapIdx = nodeIdIdxMap
                                .get(Long.valueOf(nd1.getAttributes().getNamedItem("ref").getTextContent()));

                        int nd2MapIdx = nodeIdIdxMap
                                .get(Long.valueOf(nd2.getAttributes().getNamedItem("ref").getTextContent()));

                        Node osmNd1Node = osmNodes.item(nd1MapIdx);
                        Node osmNd2Node = osmNodes.item(nd2MapIdx);
                        Double weight = getWeight(
                                osmNd1Node.getAttributes().getNamedItem("lat").getTextContent(),
                                osmNd1Node.getAttributes().getNamedItem("lon").getTextContent(),
                                osmNd2Node.getAttributes().getNamedItem("lat").getTextContent(),
                                osmNd2Node.getAttributes().getNamedItem("lon").getTextContent());
                        // get edge in csv(" " at the begining) :

                        String csvNd1Edge = csvBody.get(nd1MapIdx)[4];
                        String csvNd2Edge = csvBody.get(nd2MapIdx)[4];

                        String add1 = " " + Long.valueOf(nd2.getAttributes().getNamedItem("ref").getTextContent()) + ":"
                                + weight;
                        String add2 = " " + Long.valueOf(nd1.getAttributes().getNamedItem("ref").getTextContent()) + ":"
                                + weight;
                        csvNd1Edge += add1;
                        csvNd2Edge += add2;

                        // write to csv file
                        csvBody.get(nd1MapIdx)[4] = csvNd1Edge.trim();
                        csvBody.get(nd2MapIdx)[4] = csvNd2Edge.trim();

                    }
                }
            }
            writer.writeAll(csvBody);
            writer.flush();
            writer.close();

            csvReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Double getWeight(String srcLat, String srcLon, String desLat, String desLon) {

        double startLat = Double.parseDouble(srcLat);
        double startLon = Double.parseDouble(srcLon);

        double endLat = Double.parseDouble(desLat);
        double endLon = Double.parseDouble(desLon);

        return Haversine.distance(startLat, startLon, endLat, endLon);
    }

    public void createPlaceDBDataCSV(String osmPath, String outputPlaceDBDataCsvFilePath) {
        try {
            List<Node> nodeList = new ArrayList<>();
            Map<Long, Integer> nodeIdIdxMap = new HashMap<>();
            Document doc = buildXmlDocumentReader(osmPath);

            UpdateOSM.makeDataFromOSM(doc, nodeList, nodeIdIdxMap, null, null);

            // RS

            NodeList osmPointNodeList = doc.getElementsByTagName("node");
            List<Long> places = new ArrayList<>();
            for (int i = 0; i < osmPointNodeList.getLength(); i++) {
                Node nodei = osmPointNodeList.item(i);
                if (nodei.hasChildNodes() && nodei.getChildNodes().getLength() > 3) {

                    places.add(Long.parseLong(nodei.getAttributes().getNamedItem("id").getTextContent()));

                }
            }
            // MODIFY LOCATION

            CSVWriter writer = new CSVWriter(
                    new FileWriter(outputPlaceDBDataCsvFilePath));
            System.out.println(">>>SET DB DATA");
            for (Long elId : places) {

                Node el = nodeList.get(nodeIdIdxMap.get(elId));
                NodeList elChilds = el.getChildNodes();

                String placeName = "none";

                for (int i = elChilds.getLength() - 1; i >= 0; i--) {

                    Node tagNode = elChilds.item(i);
                    if (tagNode.getNodeName().equals("tag")
                            && tagNode.getAttributes().getNamedItem("k").getTextContent().equals("name")) {
                        placeName = tagNode.getAttributes().getNamedItem("v").getTextContent();
                        break;
                    }

                }

                if (placeName.equals("none"))
                    continue;

                String[] array = new String[] { String.valueOf(elId), placeName };
                writer.writeNext(array);
            }
            writer.close();

            System.out.println("Setup place data to csv file success fully");
        } catch (

        Exception e) {
            e.printStackTrace();
        }

    }

}