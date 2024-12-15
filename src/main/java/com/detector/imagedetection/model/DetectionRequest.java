package com.detector.imagedetection.model;

import lombok.Data;

@Data
public class DetectionRequest {
    private String label;
    private String url;
    private String filepath;
    private Boolean detectObject;
}
