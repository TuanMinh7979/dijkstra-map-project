package com.example.demo.utils;

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

    private List<NearestServiceData> nearestServiceDatas = new ArrayList<>();

    public void updateOsmXmlData(String osmFilePath) {
        System.out.println("===UPDATE OSM XML(OSM) FILE===");
        updatePlaceLonLatAndNearestServiceDatas(osmFilePath);
        addPlaceNodeToWay(osmFilePath);
        System.out.println("Update osm xml file data successfully!");
        return;
    }

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

    // func to change long and lat of place node by its way point long and lat(from
    // nearest service waypoint api)
    // build insert data list( from nearest service waypoint data )with each element
    // is {placeNodeId: placeNodeId,wayPointNearestNodeId: nearest node of wp node,
    // street name
    // }
    private void updatePlaceLonLatAndNearestServiceDatas(String osmXmlFilePathToUpdate) {
        System.out.println("___UPDATE PLACE LONG AND LAT BY PLACE'S WAY POINT LONG AND LAT ___");
        try {

            Document doc = buildXmlDocumentReader(osmXmlFilePathToUpdate);

            NodeList osmPointNodeList = doc.getElementsByTagName("node");

            for (int i = 0; i < osmPointNodeList.getLength(); i++) {
                Node nodei = osmPointNodeList.item(i);
                if (nodei.hasChildNodes() && nodei.getChildNodes().getLength() > 3) {
                    String oldlat = nodei.getAttributes().getNamedItem("lat").getTextContent();
                    String oldlong = nodei.getAttributes().getNamedItem("lon").getTextContent();
                    String urlStr = "http://router.project-osrm.org/nearest/v1/driving/" + oldlong + "," + oldlat
                            + "?number=1";
                    System.out
                            .println("Get waypoint data of place ( nearest node id and way name) in nearest service api: " + urlStr);
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
                        JSONArray waypointNearestNodes = (JSONArray) wpData.get("nodes");

                        // data
                        Long wayPointNearestNodeId = null;
                        /// data
                        for (int j = 0; j < waypointNearestNodes.length(); j++) {
                            Long curNbNodeId = Long.valueOf(String.valueOf(waypointNearestNodes.get(j)));
                            if (curNbNodeId > 0) {
                                wayPointNearestNodeId = curNbNodeId;
                                break;
                            }
                        }

                        Long placeNodeId = Long.parseLong(nodei.getAttributes().getNamedItem("id").getTextContent());
                        String dataStreetName = String.valueOf(wpData.get("name"));


                        dataStreetName = dataStreetName.isEmpty() ? "nullname" : dataStreetName;
                        nearestServiceDatas
                                .add(new NearestServiceData(placeNodeId, wayPointNearestNodeId, dataStreetName));

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

            System.out.println("Update place location by it way point location successfully");

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    public static void makeDataFromOSM(Document doc, List<Node> nodeIds, Map<Long, Integer> nodeIdIdxMap,
            List<Node> wayIds, Map<Long, Integer> wayIdIdxMap) {
        try {

            if (nodeIds != null && nodeIdIdxMap != null) {
                NodeList osmPointNodeList = doc.getElementsByTagName("node");
                for (int i = 0; i < osmPointNodeList.getLength(); i++) {
                    Node el = osmPointNodeList.item(i);

                    Long elId = Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent());
                    nodeIds.add(el);
                    nodeIdIdxMap.put(elId, i);
                }
            }

            if (wayIds != null && wayIdIdxMap != null) {
                NodeList osmWayNodeList = doc.getElementsByTagName("way");

                for (int i = 0; i < osmWayNodeList.getLength(); i++) {
                    Node el = osmWayNodeList.item(i);

                    wayIds.add(el);
                    Long elId = Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent());
                    wayIdIdxMap.put(elId, i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    // func to add to this.nodeIds and this.way
    private void addPlaceNodeToWay(String osmXmlFilePathToUpdate) {
        // setup resource
        System.out.println("___ADD UPDATED PLACE NODE TO WAY NODE___");
        try {

            List<Node> nodeIds = new ArrayList<>();
            Map<Long, Integer> nodeIdIdxMap = new HashMap<>();
            List<Node> wayIds = new ArrayList<>();
            Map<Long, Integer> wayIdIdxMap = new HashMap<>();
            Document doc = buildXmlDocumentReader(osmXmlFilePathToUpdate);
            // RS
            makeDataFromOSM(doc, nodeIds, nodeIdIdxMap, wayIds, wayIdIdxMap);

            // ADD NODEID TO WAY
            for (NearestServiceData pairI : nearestServiceDatas) {
                try {
                    Long wayPointNearestNodeId = pairI.getWaypointNearestNodeId();
                    Long placeNodeId = pairI.getPlaceNodeId();
                    String streetName = pairI.getStreetName();

                    Long wayId = getWayId(wayPointNearestNodeId, streetName);
                    if (wayId == null) {
                        System.out.println("Place waypoint id is " + wayPointNearestNodeId + " of place have id " + placeNodeId
                                + " is not in any way");
                        System.out.println("----------continue 243");
                        continue;
                    }

                    Integer wayIdx = wayIdIdxMap.get(wayId);
                    if (wayIdx == null){
                        System.out.println("----------continue 248");
                        continue;
                    }

                    Node wayPointWay = wayIds.get(wayIdx);

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
                                            .parseDouble(nodeIds.get(nodeIdIdxMap.get(ndTagItemRef)).getAttributes()
                                                    .getNamedItem("lon").getTextContent()));
                        }
                    }

                    Double firstLong = longListOrderForUpdateWay.get(0);
                    Double secondLong = longListOrderForUpdateWay.get(1);
                    Integer placeNodeIdx = nodeIdIdxMap.get(placeNodeId);
                    if (placeNodeIdx == null){
                        System.out.println("----------continue 276");
                        continue;
                    }

                    Node addPointNode = nodeIds.get(nodeIdIdxMap.get(placeNodeId));
                    Double addNodeLong = Double
                            .parseDouble(addPointNode.getAttributes().getNamedItem("lon").getTextContent());

                    Element nd = doc.createElement("nd");
                    nd.setAttribute("ref", String.valueOf(placeNodeId));
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
                } catch (Exception e) {
                    System.out.println("Exception in addPlaceNodeToWay Loop " + e.getMessage());
                    System.out.println("----------continue 313");
                    continue;
                }

            }
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(osmXmlFilePathToUpdate);
            tf.transform(source, result);
            System.out.println("Add updated place  to way node successfully");

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Long getWayId(Long wayPointNearestNodeId, String streetNamePar) {
        String urlStr = "https://api.openstreetmap.org/api/0.6/node/" + wayPointNearestNodeId + "/ways";
        System.out.println("Get way data of place's waypoint id : " + wayPointNearestNodeId + " at:  " + urlStr);
        URL url = null;

        try {
            url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());

            NodeList ways = doc.getElementsByTagName("way");
            String wayIdStr = "";

            if (ways.getLength() == 0)
                return null;
            for (int i = 0; i < ways.getLength(); i++) {
                Node wayItem = ways.item(i);
                NodeList wayItemChilds = wayItem.getChildNodes();

                for (int j = wayItemChilds.getLength() - 1; j >= 0; j--) {

                    Node childNode = wayItemChilds.item(j);

                    if (childNode.getNodeName().equals("tag")) {


                        if (!streetNamePar.equals("nullname")
                                && childNode.getAttributes().getNamedItem("k").getTextContent().equals("name")
                                && (childNode.getAttributes().getNamedItem("v").getTextContent().equals(streetNamePar)
                                        || streetNamePar.indexOf(
                                                childNode.getAttributes().getNamedItem("v").getTextContent()) != -1)) {
                            wayIdStr = wayItem.getAttributes().getNamedItem("id").getTextContent();

                            return Long.parseLong(wayIdStr);

                        }

                        if (streetNamePar.equals("nullname")
                                && !childNode.getAttributes().getNamedItem("k").getTextContent().equals("name")) {
                            wayIdStr = wayItem.getAttributes().getNamedItem("id").getTextContent();

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

}