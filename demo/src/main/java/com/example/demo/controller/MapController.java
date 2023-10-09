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
    private String activeMapName = "default";


    @GetMapping("maps")
    public List<String> allMap() {
        File directoryPath = new File(DATA_PATH + "/osm");
        //List of all files and directories
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

    @GetMapping("test-update-osm")
    public String testUpdateOSM() {
        String osmPath = DATA_PATH + "/osm/" + activeMapName + ".xml";
        updateOSM.updateOsmXmlData(osmPath);
        return "thanhcong";
    }


    @GetMapping("test-create-graphcsv")
    public String testCreateGraphCSV() {
        String osmPath = DATA_PATH + "/osm/" + activeMapName + ".xml";
        String newGraphDataCsvPath = DATA_PATH + "/graphcsv/" + activeMapName + ".csv";
        osmToCSV.toCSVData(osmPath, newGraphDataCsvPath);
        return "thanhcong";
    }


    @GetMapping("map/{mapname}")
    public Map<String, String> getMap(@PathVariable String mapname) {
        adjListGraph.setupGraph(DATA_PATH + "/graphcsv/" + mapname + ".csv");
        placeData.setup(DATA_PATH + "/dbcsv/" + mapname + ".csv");
        return adjListGraph.getMiddlePlace();
    }

    public void hdleSetup() {
        adjListGraph.setupGraph(DATA_PATH + "/graphcsv/" + activeMapName + ".csv");
        placeData.setup(DATA_PATH + "/dbcsv/" + activeMapName + ".csv");

    }


    // map/api/search

    @GetMapping("search")
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


    @PostMapping("add-new-map")
    public String addUpdateAndCreateDbCsvDataFromXml(@RequestParam("file") MultipartFile file) {
        File savedFile = uploadUtil.handelUploadFile(file);
        String osmPath = savedFile.getPath();
        updateOSM.updateOsmXmlData(osmPath);
        osmToCSV.toCSVData(osmPath, osmPath.replace("osm", "graphcsv"));
        return "success";
    }

    @GetMapping("viewdata")
    @ResponseBody
    public Map<Long, Point> viewData() {
        Map<Long, Point> data = adjListGraph.getData();
        return data;
    }

    @GetMapping("setup/delete-csvdata/{dataFileName}")
    @ResponseBody
    public ResponseEntity<String> deleteMap(@PathVariable String dataFileName) {
        File graphCsvPath = new File(
                DATA_PATH + "\\src\\main\\webapp\\mapsimulator\\graphcsv\\" + dataFileName + ".csv");
        if (graphCsvPath.exists())
            graphCsvPath.delete();

        File dbCsvPath = new File(DATA_PATH + "\\src\\main\\webapp\\mapsimulator\\dbcsv\\" + dataFileName + ".csv");
        if (dbCsvPath.exists())
            dbCsvPath.delete();

        File graphXmlPath = new File(DATA_PATH + "\\src\\main\\webapp\\mapsimulator\\osm\\" + dataFileName + ".xml");
        if (graphXmlPath.exists())
            graphXmlPath.delete();
        File graphOsmPath = new File(DATA_PATH + "\\src\\main\\webapp\\mapsimulator\\osm\\" + dataFileName + ".osm");
        if (graphOsmPath.exists())
            graphOsmPath.delete();
        return new ResponseEntity<>("delete map success", HttpStatus.OK);
    }


}