package com.example.demo.controller;

import com.example.demo.algo.DijkstraSearchAlgo;
import com.example.demo.data.PlaceData;
import com.example.demo.data.PlaceDto;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
@Setter
@Getter
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class MapController {

    private final DijkstraSearchAlgo dijkstraSearchAlgo;
    private final AdjListGraph adjListGraph;
    private final PlaceData placeData;
    private final UploadUtil uploadUtil;
    private final UpdateOSM updateOSM;
    private final OSMToCSV osmToCSV;

    private final String DATA_PATH = System.getProperty("user.dir") + "/src/main/mapdata";
    // private String activeMapName = "default";

    private String createOsmXmlPath(String filename) {
        return DATA_PATH + "/osm/" + filename + ".xml";
    }

    private String createPLacesCsv(String filename) {
        return DATA_PATH + "/dbcsv/" + filename + ".csv";
    }

    private String createGraphCsv(String filename) {
        return DATA_PATH + "/graphcsv/" + filename + ".csv";
    }

    @GetMapping("maps")
    public List<String> allMap() {
        File directoryPath = new File(DATA_PATH + "/osm");
        // List of all files and directories
        String contents[] = directoryPath.list();
        List<String> allMap = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            allMap.add(contents[i].substring(0, contents[i].lastIndexOf(".")));
        }

        return allMap;
    }

    @GetMapping("places")
    public List<PlaceDto> searchPlace() {

        return placeData.getPlaceDtoList();
    }

    // @GetMapping("test-update-osm")
    // public String testUpdateOSM() {
    // String osmPath = createOsmXmlPath(activeMapName);
    // updateOSM.updateOsmXmlData(osmPath);
    // return "thanhcong";
    // }

    // @GetMapping("test-create-graphcsv")
    // public String testCreateGraphCSV() {
    // String osmPath = createOsmXmlPath(activeMapName);
    // String newGraphDataCsvPath = createGraphCsv(activeMapName);
    // osmToCSV.toCSVData(osmPath, newGraphDataCsvPath);
    // return "thanhcong";
    // }

    @GetMapping("map/{mapname}")
    public Map<String, String> getMap(@PathVariable String mapname) {
        adjListGraph.setupGraph(createGraphCsv(mapname));
        placeData.setup(createPLacesCsv(mapname));
        return adjListGraph.getMiddlePlace();
    }

    // map/api/search

    @GetMapping("search")
    @ResponseBody
    public List<PointDto> getShortestPath(
            @RequestParam(value = "source") String srcStr,
            @RequestParam(value = "destination") String desStr) {
        System.out.println("--_____________SEARCH " + srcStr + "--->" + desStr);
        return dijkstraSearchAlgo.findShortestPath(Long.parseLong(srcStr), Long.parseLong(desStr));
    }

    @PostMapping("add-new-map")
    public Map<String, String> addUpdateAndCreateDbCsvDataFromXml(@RequestParam("file") MultipartFile file) {
        File savedFile = uploadUtil.handelUploadFile(file);
        String osmPath = savedFile.getPath();
        String mapName = savedFile.getName().substring(0, savedFile.getName().lastIndexOf("."));
        updateOSM.updateOsmXmlData(osmPath);
        osmToCSV.toCSVData(osmPath, createGraphCsv(mapName));
        osmToCSV.createPlaceDBDataCSV(osmPath, createPLacesCsv(mapName));
        return new HashMap<String, String>() {
            {
                put("filename", mapName);
            }

        };
    }

    @GetMapping("viewdata")
    @ResponseBody
    public Map<Long, Point> viewData() {
        Map<Long, Point> data = adjListGraph.getData();
        return data;
    }

    @DeleteMapping("map/{mapName}")
    @ResponseBody
    public ResponseEntity<String> deleteMap(@PathVariable String mapName) {
        File graphCsvFile = new File(createGraphCsv(mapName));
        if (graphCsvFile.exists())
            graphCsvFile.delete();

        File placesCsvFile = new File(createPLacesCsv(mapName));
        if (placesCsvFile.exists())
            placesCsvFile.delete();

        File osmXmlFile = new File(createOsmXmlPath(mapName));
        if (osmXmlFile.exists())
            osmXmlFile.delete();

        return new ResponseEntity<>("delete map success", HttpStatus.OK);
    }

}