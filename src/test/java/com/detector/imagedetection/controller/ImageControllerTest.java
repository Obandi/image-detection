package com.detector.imagedetection.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.detector.imagedetection.exceptions.model.BadRequestException;
import com.detector.imagedetection.exceptions.model.InternalServerErrorException;
import com.detector.imagedetection.model.DetectionRequest;
import com.detector.imagedetection.model.Image;
import com.detector.imagedetection.repository.ImageRepository;
import com.detector.imagedetection.service.ImageService;

public class ImageControllerTest {

    @Mock
    private ImageService imageServiceMock;

    @Mock
    private ImageRepository imageRepositoryMock;


    @InjectMocks
    private ImageController imageController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetImageById_Is_Successful() {
        Image image = new Image();
        image.setId(1L);
        image.setLabel("test label");
        image.setTags(null);

        when(imageServiceMock.getImageById(anyLong())).thenReturn(image);

        ResponseEntity<Image> response = imageController.getImageById(1L);

        assertEquals(ResponseEntity.ok(image), response);
    }

    @Test
    void testGetImageById_Is_Not_Successful() {

        when(imageServiceMock.getImageById(anyLong())).thenReturn(null);

        ResponseEntity<Image> response = imageController.getImageById(1L);

        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    void testGetImageByTags_Is_Successful() {
        Image image = new Image();
        image.setId(1L);
        image.setLabel("test label");
        image.setTags(null);

        when(imageServiceMock.getImagesByTags(anyString())).thenReturn(List.of(image));

        ResponseEntity<List<Image>> response = imageController.getImagesByTags("Hello World");

        assertEquals(ResponseEntity.ok(List.of(image)), response);
    }

    @Test
    void testGetImageByTags_Is_Not_Successful() {
        Image image = new Image();
        image.setId(1L);
        image.setLabel("test label");
        image.setTags(null);

        when(imageServiceMock.getImagesByTags(anyString())).thenThrow(new InternalServerErrorException("Bad Test"));

        try {
            imageController.getImagesByTags("Hello World");
        } catch (InternalServerErrorException e) {
            System.out.println(e);
            assertEquals("Bad Test", e.getMessage());
        }
    }

    @Test
    void testGetImageBy_Is_Successful() {
        Image image = new Image();
        image.setId(1L);
        image.setLabel("test label");
        image.setTags(null);

        when(imageServiceMock.getImages()).thenReturn(List.of(image));

        ResponseEntity<List<Image>> response = imageController.getImages();

        assertEquals(ResponseEntity.ok(List.of(image)), response);
    }

    @Test
    void testGetImageBy_Is_Not_Successful() {
        Image image = new Image();
        image.setId(1L);
        image.setLabel("test label");
        image.setTags(null);

        when(imageServiceMock.getImages()).thenThrow(new InternalServerErrorException("JSON serialization exception"));

        try {
            imageController.getImages();
        } catch (InternalServerErrorException e) {
            System.out.println(e);
            assertEquals("JSON serialization exception", e.getMessage());
        }
    }

    @Test
    void testDetectImage_Is_Successful() {
        DetectionRequest request = new DetectionRequest();
        request.setDetectObject(true);
        request.setFilepath("test.jpg");

        Image image = new Image();
        image.setId(2L);
        image.setLabel("test label");
        image.setTags(null);

        when(imageServiceMock.detectImageFromFile(request.getFilepath(), null, request.getDetectObject()))
        .thenReturn(image);

        ResponseEntity<Image> response = imageController.detectImage(request);

        assertEquals(ResponseEntity.ok(image), response);
    }

    @Test
    void testDetectImage_Is_Not_Successful() {
        DetectionRequest request = new DetectionRequest();
        request.setDetectObject(true);

        try {
            imageController.detectImage(request);
        } catch (BadRequestException e) {
            System.out.println(e);
            assertEquals("Filepath or URL not present in request", e.getMessage());
        }
    }
    
}
