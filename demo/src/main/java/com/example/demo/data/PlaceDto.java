package com.example.demo.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class PlaceDto {
    private Long id;
    private String name;

    public PlaceDto(Long id, String name) {
        this.id = id;
        this.name = name;

    }
}

