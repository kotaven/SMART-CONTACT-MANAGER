package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.About;

public interface AboutRepository extends JpaRepository<About, Long> {
}
