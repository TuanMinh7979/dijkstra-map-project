package com.example.demo.data;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.pojo.Edge;
import com.example.demo.pojo.Point;
@Component
@Getter

public class AdjListGraph {
    //nodeid => index in list
    private Map<Long, Point> data = new HashMap<>();



    public void setupGraph(String csvFilePath) {
        try {
            CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFilePath))
                    .build();
            List<String[]> csvBody = csvReader.readAll();
//            System.out.println("Data size: " + csvBody.size());
            if (!data.isEmpty()) {
                data = new HashMap<>();
            }


            for (String[] csvLine : csvBody) {
                Point point = new Point();
                point.setId(Long.valueOf(csvLine[1]));
                point.setLat(csvLine[2]);
                point.setLon(csvLine[3]);


                String fileEdgesStr = csvLine[4];
                List<Edge> edgesData = new ArrayList<>();
                List<String> fileEdges = new ArrayList<>(Arrays.asList(fileEdgesStr.split(" ")));
                for (String edgei : fileEdges) {
                    String[] edgePair = edgei.split(":");
                    Edge newEdge = new Edge();
                    newEdge.setNeighBourId(Long.valueOf(edgePair[0]));
                    newEdge.setWeight(Double.valueOf(edgePair[1]));
                    edgesData.add(newEdge);
                }
                point.setEdges(edgesData);


                data.put(Long.valueOf(csvLine[1]), point);

            }
            csvReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Map<String, String> getMiddlePlace(){
        List<Long> keyList = this.data.keySet().stream().collect(Collectors.toList());
        Long middleKey = keyList.get(this.data.size() / 2);
        Point middleItem = this.data.get(middleKey);
        return new HashMap<String, String>() {{
            put("lng", middleItem.getLon());
            put("lat", middleItem.getLat());

        }};
    }

}

