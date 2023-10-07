package com.example.demo.utils;


import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;

import java.util.*;


@Slf4j

@Getter
@Setter

@Component
@RequiredArgsConstructor
public class UpdateOSM {

    private List<Node> pointIds = new ArrayList<>();
    private Map<Long, Integer> pointIdIdxMap = new HashMap<>();

    private List<Node> wayIds = new ArrayList<>();
    private Map<Long, Integer> wayIdIdxMap = new HashMap<>();

    private List<InsertData> insertDatas = new ArrayList<>();

    public void updateOsmXmlData(String osmFilePath) {
        System.out.println("UPDATE OSM XML FILE:");
        setupPlaceLocation(osmFilePath);
        addPlaceNodeToWay(osmFilePath);
        System.out.println("Update osm xml file data successfully!");
        return;
    }

    // func to change long and lat of place node by its way point long and lat(from
    // nearest service waypoint api)
    // build insert data list( from nearest service waypoint data )with each element
    // is {addNodeId: placeNodeId,dataNbNodeId: nearest node of wp node, street name
    // }
    private void setupPlaceLocation(String osmXmlFilePathToUpdate) {
        System.out.println(">>>SETUP PLACE LOCATION");
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            InputStream istStream = new FileInputStream(osmXmlFilePathToUpdate);

            Reader reader = new InputStreamReader(istStream, "UTF-8");
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(is);

            NodeList osmPointNodeList = doc.getElementsByTagName("node");

            System.out.println("Node list size: " + osmPointNodeList.getLength());
            for (int i = 0; i < osmPointNodeList.getLength(); i++) {
                Node nodei = osmPointNodeList.item(i);
                if (nodei.hasChildNodes() && nodei.getChildNodes().getLength() > 3) {
                    String oldlat = nodei.getAttributes().getNamedItem("lat").getTextContent();
                    String oldlong = nodei.getAttributes().getNamedItem("lon").getTextContent();
                    String urlStr = "http://router.project-osrm.org/nearest/v1/driving/" + oldlong + "," + oldlat
                            + "?number=1";
                    System.out.println(">>>>Url get waypoint location: " + urlStr);
                    URL url = null;
                    try {
                        url = new URL(urlStr);

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();
                        StringBuilder jsonStrBuilder = new StringBuilder();

                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        String line;
                        while ((line = rd.readLine()) != null) {
                            jsonStrBuilder.append(line);
                        }
                        rd.close();

                        String jsonStr = jsonStrBuilder.toString();

                        // GET DATA

                        JSONObject rsJsonObj = new JSONObject(jsonStr);
                        JSONArray waypoints = (JSONArray) rsJsonObj.get("waypoints");
                        JSONObject wpData = (JSONObject) waypoints.get(0);

                        JSONArray nwNodes = (JSONArray) wpData.get("nodes");

                        // data
                        Long dataNbNodeId = null;
                        /// data
                        for (int j = 0; j < nwNodes.length(); j++) {
                            Long curNbNodeId = Long.valueOf(String.valueOf(nwNodes.get(j)));
                            if (curNbNodeId > 0) {
                                dataNbNodeId = curNbNodeId;
                                break;
                            }
                        }

                        Long addNodeId = Long.parseLong(nodei.getAttributes().getNamedItem("id").getTextContent());
                        String dataStreetName = String.valueOf(wpData.get("name"));

                        System.out.println("____STREETNAME là: " + dataStreetName);
                        dataStreetName = dataStreetName.isEmpty() ? "nullname" : dataStreetName;
                        insertDatas.add(new InsertData(addNodeId, dataNbNodeId, dataStreetName));

                        JSONArray location = (JSONArray) wpData.get("location");
                        // data
                        String wpLong = String.valueOf(location.get(0));
                        String wpLat = String.valueOf(location.get(1));
                        /// data
                        // GET DATA

                        // MODIFY LOCATION

                        // set lat and log of waypoint to current place node
                        nodei.getAttributes().getNamedItem("lat").setTextContent(wpLat);
                        nodei.getAttributes().getNamedItem("lon").setTextContent(wpLong);

                        // MODIFY LOCATION

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    }
                }
            }

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(osmXmlFilePathToUpdate);

            tf.transform(source, result);
            System.out.println(">>>Setup place location successfully");

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

    }

    public void makeRSFromOSM(String osmXmlFilePathToUpdate) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            InputStream istStream = new FileInputStream(osmXmlFilePathToUpdate);

            InputSource is = new InputSource(new InputStreamReader(istStream, "UTF-8"));
            is.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(is);

            NodeList osmPointNodeList = doc.getElementsByTagName("node");

            for (int i = 0; i < osmPointNodeList.getLength(); i++) {
                Node el = osmPointNodeList.item(i);

                Long elId = Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent());
                this.pointIds.add(el);
                this.pointIdIdxMap.put(elId, i);
            }

            NodeList osmWayNodeList = doc.getElementsByTagName("way");

            for (int i = 0; i < osmWayNodeList.getLength(); i++) {
                Node el = osmWayNodeList.item(i);

                this.wayIds.add(el);
                Long elId = Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent());
                this.wayIdIdxMap.put(elId, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // func to add to this.pointIds and this.way
    private void addPlaceNodeToWay(String osmXmlFilePathToUpdate) {
        // setup resource
        System.out.println(">>>ADD PLACE NODE TO WAY");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            InputStream istStream = new FileInputStream(osmXmlFilePathToUpdate);

            InputSource is = new InputSource(new InputStreamReader(istStream, "UTF-8"));
            is.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(is);

            // RS

            // ADD NODEID TO WAY
            for (InsertData pairI : insertDatas) {

                Long nbNodeId = pairI.getNbNodeId();
                Long addNodeId = pairI.getAddNodeId();
                String streetName = pairI.getStreetName();

                Long wayId = getWayId(nbNodeId, streetName);
                if (wayId == null) {
                    log.warn(">>>Neighbor " + nbNodeId + " of node " + addNodeId + "'s waypoint is not in any way");
                    continue;
                }

                Integer wayIdx = wayIdIdxMap.get(wayId);

                Node wayPointWay = this.wayIds.get(wayIdx);
                NodeList wayPointWayChilds = wayPointWay.getChildNodes();

                List<Node> wayPointWayChildNdTagList = new ArrayList<>();

                List<Double> longListOrderForUpdateWay = new ArrayList<>();
                for (int k = 0; k < wayPointWayChilds.getLength(); k++) {
                    Node ndTagItem = wayPointWayChilds.item(k);
                    if (ndTagItem.getNodeName().equals("nd")) {
                        wayPointWayChildNdTagList.add(ndTagItem);
                        Long ndTagItemRef = Long
                                .parseLong(ndTagItem.getAttributes().getNamedItem("ref").getTextContent());
                        longListOrderForUpdateWay
                                .add(Double
                                        .parseDouble(this.pointIds.get(pointIdIdxMap.get(ndTagItemRef)).getAttributes()
                                                .getNamedItem("lon").getTextContent()));
                    }
                }

                Double firstLong = longListOrderForUpdateWay.get(0);
                Double secondLong = longListOrderForUpdateWay.get(1);

                Node addPointNode = this.pointIds.get(pointIdIdxMap.get(addNodeId));
                Double addNodeLong = Double
                        .parseDouble(addPointNode.getAttributes().getNamedItem("lon").getTextContent());

                Element nd = doc.createElement("nd");
                nd.setAttribute("ref", String.valueOf(addNodeId));
                nd.setAttribute("lon", String.valueOf(addNodeLong));
                longListOrderForUpdateWay.add(addNodeLong);

                if (firstLong > secondLong) {
                    // decrease
                    Collections.sort(longListOrderForUpdateWay, Comparator.reverseOrder());

                } else {
                    // increase
                    Collections.sort(longListOrderForUpdateWay);

                }
                int addedEleIdx = longListOrderForUpdateWay.indexOf(addNodeLong);

                Node posNode = null;
                if (addedEleIdx < (longListOrderForUpdateWay.size() - 1)) {
                    posNode = wayPointWayChildNdTagList.get(addedEleIdx);
                    wayPointWay.insertBefore(nd, posNode);
                } else if (addedEleIdx == (longListOrderForUpdateWay.size() - 1)) {
                    wayPointWay.appendChild(nd);
                }
            }
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(osmXmlFilePathToUpdate);
            tf.transform(source, result);
            System.out.println(">>>Add new place node to way successfully");

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public Long getWayId(Long nbNodeId, String streetName) {
        String urlStr = "https://api.openstreetmap.org/api/0.6/node/" + nbNodeId + "/ways";
        System.out.println("way api of a node: " + urlStr);
        URL url = null;

        try {
            url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());

            NodeList ways = doc.getElementsByTagName("way");
            String wayIdStr = "";

            System.out.println(">>>>>num of way from " + nbNodeId + " is " + ways.getLength());

            if (ways.getLength() == 0)
                return null;
            for (int i = 0; i < ways.getLength(); i++) {
                Node wayItem = ways.item(i);
                NodeList wayItemChilds = wayItem.getChildNodes();

                for (int j = wayItemChilds.getLength() - 1; j >= 0; j--) {

                    Node childNode = wayItemChilds.item(j);

                    if (childNode.getNodeName().equals("tag")) {

                        System.out.println(childNode.getNodeName() + "INFO:" + j + "  streetname" + streetName
                                + " ---key: " + childNode.getAttributes().getNamedItem("k").getTextContent()
                                + "  ---value : " + childNode.getAttributes().getNamedItem("v").getTextContent());

                        if (childNode.getAttributes().getNamedItem("k").getTextContent().equals("name")) {
                            System.out.println("_----------------------street in api: "
                                    + childNode.getAttributes().getNamedItem("k").getTextContent().equals("name"));
                        }
                        if (!streetName.equals("nullname")
                                && childNode.getAttributes().getNamedItem("k").getTextContent().equals("name")
                                && (childNode.getAttributes().getNamedItem("v").getTextContent().equals(streetName)
                                        || streetName.indexOf(
                                                childNode.getAttributes().getNamedItem("v").getTextContent()) != -1)) {
                            wayIdStr = wayItem.getAttributes().getNamedItem("id").getTextContent();
                            System.out.println(">>>>>Way id is " + wayIdStr + " in case 1");
                            return Long.parseLong(wayIdStr);

                        }

                        if (streetName.equals("nullname")
                                && !childNode.getAttributes().getNamedItem("k").getTextContent().equals("name")) {
                            wayIdStr = wayItem.getAttributes().getNamedItem("id").getTextContent();
                            System.out.println(">>>>>Way id is " + wayIdStr + " in case 2");
                            return Long.parseLong(wayIdStr);

                        }

                    }

                }

            }
            // if after all way from this node that do not return any time throw exception
            // if (wayIdStr.isEmpty()) throw new RuntimeException("Way id is not found");
            System.out.println("No way found and return null");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void createPlaceDBDataCSV(String osmPath, String outputPlaceDBDataCsvFilePath) {
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            InputStream istStream = new FileInputStream(osmPath);

            Reader reader = new InputStreamReader(istStream, "UTF-8");
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(is);

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

                Node el = this.pointIds.get(pointIdIdxMap.get(elId));
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
                // place.setNodeId(nodeId);
                // place.setName(placeName);
                System.out.println("Place node Id: " + elId + " place name: " + placeName);
                String[] array = new String[] { String.valueOf(elId), placeName };
                writer.writeNext(array);
            }
            writer.close();
            System.out.println("Setup place db data to csv file success fully");
        } catch (

        Exception e) {
            e.printStackTrace();
        }

    }

    public void insertPlaceToDB(String csvPlaceDbPath) {
        // placeRepo.deleteAll();
        try {
            CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvPlaceDbPath)).build();
            List<String[]> csvBody = csvReader.readAll();

            for (String[] placei : csvBody) {
                System.out.println("insert nodeId: " + placei[0] + " _ " + "place name: " + placei[1]);
                // try {
                // placeRepo.save(new Place(Long.parseLong(placei[0]), placei[1]));
                // } catch (Exception e) {

                // e.printStackTrace();
                // Random r = new Random();
                // placeRepo.save(new Place(Long.parseLong(placei[0]), String.valueOf(placei[1]
                // + r.nextInt(100))));
                // }
            }
            csvReader.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

            e.printStackTrace();
        }

        System.out.println(">>>Insert Node data to db successfully");
    }

}