package com.detector.imagedetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.detector.imagedetection.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {}

