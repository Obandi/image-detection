package com.detector.imagedetection.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.detector.imagedetection.model.DetectionRequest;
import com.detector.imagedetection.model.Image;
import com.detector.imagedetection.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/detection")
public class ImageController {
    private final ImageService imageService;

    
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "Retrieve image by Id")
    @ApiResponses(value = { 
    @ApiResponse(responseCode = "200", description = "Image found", 
        content = { @Content(mediaType = "application/json", 
        schema = @Schema(implementation = Image.class)) }),
        @ApiResponse(responseCode = "404", description = "Image not found", 
        content = @Content),
    @ApiResponse(responseCode = "500", description = "Internal Server Error", 
        content = @Content) })
    @GetMapping("/images/{id}")
    public ResponseEntity<?> getImageById(@Parameter(description = "Id of image")@PathVariable("id") Long id) {
        if(null == imageService.getImageById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(imageService.getImageById(id));
    }

    @Operation(summary = "Retrieve all images by tags")
    @ApiResponses(value = { 
    @ApiResponse(responseCode = "200", description = "Successful Response", 
        content = { @Content(mediaType = "application/json", 
        schema = @Schema(implementation = Image.class)) }),
    @ApiResponse(responseCode = "500", description = "Internal Server Error", 
        content = @Content) })
    @GetMapping("/images")
    public ResponseEntity<?> getImagesByTags(@Parameter(description = "Comma delimited String of tags")@RequestParam(name = "objects", required = true) String objects) {
        if(StringUtils.isEmpty(objects)) {
            return ResponseEntity.badRequest().body("No query parameters detected");
        }
        return ResponseEntity.ok().body(imageService.getImagesByTags(objects));
    }
    
    @Operation(summary = "Retrieve all images")
    @ApiResponses(value = { 
    @ApiResponse(responseCode = "200", description = "Successful Response", 
        content = { @Content(mediaType = "application/json", 
        schema = @Schema(implementation = Image.class)) }),
    @ApiResponse(responseCode = "500", description = "Internal Server Error", 
        content = @Content) })
    @GetMapping("/images/")
    public ResponseEntity<?> getImages() {
        return ResponseEntity.ok().body(imageService.getImages());
    }
    

    @Operation(summary = "Detect provided Image")
    @ApiResponses(value = { 
    @ApiResponse(responseCode = "200", description = "Successful image detection processing", 
        content = { @Content(mediaType = "application/json", 
        schema = @Schema(implementation = Image.class)) }),
    @ApiResponse(responseCode = "400", description = "Bad Request", 
    content = @Content),
    @ApiResponse(responseCode = "500", description = "Internal Server Error", 
        content = @Content) })
    @PostMapping("/images")
    public ResponseEntity<?> detectImage(@Parameter(description = "Image detection request body")@RequestBody DetectionRequest request) {
        //Check if image url or filepath is present when object detection is true
        if(StringUtils.isEmpty(request.getFilepath()) && StringUtils.isEmpty(request.getUrl())
        && Boolean.TRUE.equals(request.getDetectObject())) {
            return ResponseEntity.badRequest().body("No image specified");
        }

        //Process object detection request
        Image imageResponse = null;
        if(StringUtils.isNotEmpty(request.getFilepath())) {
            try {
                imageResponse = imageService.detectImageFromFile(request.getUrl(), request.getLabel(), request.getDetectObject());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        } else if(StringUtils.isNotEmpty(request.getUrl())) {
            try {
                imageResponse = imageService.detectImageFromUrl(request.getUrl(), request.getLabel(), request.getDetectObject());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }
        return ResponseEntity.ok().body(imageResponse);
    }
    
}
