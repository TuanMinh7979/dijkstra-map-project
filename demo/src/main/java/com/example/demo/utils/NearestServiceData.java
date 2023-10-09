package com.example.demo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class NearestServiceData {
    private Long placeNodeId;
    private Long waypointNearestNodeId;
    private String streetName;
}
