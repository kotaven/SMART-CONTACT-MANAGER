package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId")
    List<Contact> findContactsByUser(@Param("userId") int userId);

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.user.id = :userId")
    long countByUser(@Param("userId") int userId);
    
    // search
    public List<Contact> findByNameContainingAndUser(String name,User user);
}
