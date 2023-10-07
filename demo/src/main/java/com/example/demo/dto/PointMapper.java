package com.example.demo.dto;



import org.springframework.stereotype.Component;

import com.example.demo.pojo.Point;

@Component
public class PointMapper {
    public PointDto toDto(Point model) {

        PointDto pointDto = new PointDto();
        pointDto.setId(model.getId());
        pointDto.setLat(model.getLat());
        pointDto.setLon(model.getLon());
        return pointDto;
    }
}