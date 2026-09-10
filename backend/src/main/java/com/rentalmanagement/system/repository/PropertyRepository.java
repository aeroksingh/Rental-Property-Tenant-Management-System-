package com.rentalmanagement.system.repository;

import com.rentalmanagement.system.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerId(Long ownerId);
    Optional<Property> findByIdAndOwnerId(Long id, Long ownerId);
}
