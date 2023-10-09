package com.example.demo.data;


import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.Getter;
import org.springframework.stereotype.Component;


import java.io.FileReader;

import java.util.*;
@Component
@Getter


public class PlaceData {

    private List<PlaceDto> placeDtoList = new ArrayList<>();

    public void setup(String csvFilePath) {
        if (!placeDtoList.isEmpty()) {
            placeDtoList = new ArrayList<>();
        }
        try {
            CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFilePath)).build();
            List<String[]> csvBody = csvReader.readAll();

            for (String[] placei : csvBody) {
                this.placeDtoList.add( new PlaceDto(Long.valueOf(placei[0]), placei[1]));

            }
            csvReader.close();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}
