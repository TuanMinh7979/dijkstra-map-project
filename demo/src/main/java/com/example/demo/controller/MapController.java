package com.example.demo.controller;

import com.example.demo.algo.DijkstraSearchAlgo;
import com.example.demo.dto.PointDto;
import com.example.demo.data.AdjListGraph;
import com.example.demo.pojo.Point;

import com.example.demo.utils.OSMToCSV;
import com.example.demo.utils.UpdateOSM;
import com.example.demo.utils.UploadUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("map")
@RequiredArgsConstructor
@Setter
@Getter
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class MapController {

    private final DijkstraSearchAlgo dijkstraSearchAlgo;
    private final AdjListGraph adjListGraph;
    private final UploadUtil uploadUtil;
    private final UpdateOSM updateOSM;
    private final OSMToCSV osmToCSV;


    private final String PROJECT_ROOT = System.getProperty("user.dir");
    private String graphPath = PROJECT_ROOT + "/src/main/mapdata/graphcsv/ct2.csv";

    @RequestMapping("")

    public String index(Model model) {

        Map<Long, Point> data = adjListGraph.getData();
        List<Long> keyList = data.keySet().stream().collect(Collectors.toList());

        System.out.println(data.size());
        return "/mapsimulator/map";
    }

    @GetMapping("setup")
    public String setupIndex(Model model) {

        return "/mapsimulator/setup";
    }

    @GetMapping("test-update-osm")
    public String updateOSM() {
     
        String osmPath= PROJECT_ROOT+"/src/main/mapdata/osm/ct2.xml";
        updateOSM.updateOsmXmlData(osmPath);
        // for database

        String newPlaceDbCsvPath = PROJECT_ROOT + "/src/main/mapdata/dbcsv/ct2.csv";
        // updateOSM.createPlaceDBDataCSV(newPlaceDbCsvPath);




        return "thanhcong";
    }
    @GetMapping("test-create-dbcsv")
    public String createDBCsv() {

        String osmPath= PROJECT_ROOT+"/src/main/mapdata/osm/ct2.xml";


        String newPlaceDbCsvPath = PROJECT_ROOT + "/src/main/mapdata/dbcsv/ct2.csv";
        updateOSM.makeRSFromOSM(osmPath);
        updateOSM.createPlaceDBDataCSV(osmPath,newPlaceDbCsvPath);



        return "thanhcong";
    }


    @GetMapping("test-create-graphcsv")
    public String createGraphCSV() {

        String osmPath= PROJECT_ROOT+"/src/main/mapdata/osm/ct2.xml";




        String newGraphDataCsvPath = PROJECT_ROOT + "/src/main/mapdata/graphcsv/ct2.csv";
        osmToCSV.toCSVData(osmPath, newGraphDataCsvPath);

        return "thanhcong";
    }


    public void hdleSetupGraph() {
        adjListGraph.setupGraph(graphPath);
        // System.out.println(">>>Setup csv to graph success");

    }

    // map/api/search

    @GetMapping("api/search")
    @ResponseBody
    public List<PointDto> getShortestPath(
            @RequestParam(value = "source") String srcStr,
            @RequestParam(value = "destination") String desStr) {
        // Long srcNodeId = placeRepo.getNodeIdByName(srcStr);
        // Long desNodeId = placeRepo.getNodeIdByName(desStr);
        // System.out.println("SOURCE NODE ID: " + srcNodeId);
        // System.out.println("DESTINATION NODE ID: " + desNodeId);

        return dijkstraSearchAlgo.findShortestPath(Long.parseLong(srcStr), Long.parseLong(desStr));
    }

    @GetMapping("api/viewdata")
    @ResponseBody
    public Map<Long, Point> viewData() {
        Map<Long, Point> data = adjListGraph.getData();

        return data;
    }

    // Ajax

    // map/ajax/place-search-data


    @GetMapping(value = "/customer")
    public String showCreateTripPage(Model model) {
        Map<Long, Point> data = adjListGraph.getData();
        List<Long> keyList = data.keySet().stream().collect(Collectors.toList());
        Long middleKey = keyList.get(data.size() / 2);
        Point middleItem = data.get(middleKey);

        model.addAttribute("longitude", middleItem.getLon());
        model.addAttribute("latitude", middleItem.getLat());
        return "/mapsimulator/customer-map";
    }

    @GetMapping("/setup/delete-csvdata/{dataFileName}")
    @ResponseBody
    public ResponseEntity<String> deleteMap(@PathVariable String dataFileName) {
        File graphCsvPath = new File(
                PROJECT_ROOT + "\\src\\main\\webapp\\mapsimulator\\graphcsv\\" + dataFileName + ".csv");
        if (graphCsvPath.exists())
            graphCsvPath.delete();

        File dbCsvPath = new File(PROJECT_ROOT + "\\src\\main\\webapp\\mapsimulator\\dbcsv\\" + dataFileName + ".csv");
        if (dbCsvPath.exists())
            dbCsvPath.delete();

        File graphXmlPath = new File(PROJECT_ROOT + "\\src\\main\\webapp\\mapsimulator\\osm\\" + dataFileName + ".xml");
        if (graphXmlPath.exists())
            graphXmlPath.delete();
        File graphOsmPath = new File(PROJECT_ROOT + "\\src\\main\\webapp\\mapsimulator\\osm\\" + dataFileName + ".osm");
        if (graphOsmPath.exists())
            graphOsmPath.delete();
        return new ResponseEntity<>("delete map success", HttpStatus.OK);
    }

    @RequestMapping("maptest")
    public String indexTest(Model model) {
        return "/mapsimulator/maptest";
    }

}