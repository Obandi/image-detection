package com.detector.imagedetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.detector.imagedetection.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
    
} 
