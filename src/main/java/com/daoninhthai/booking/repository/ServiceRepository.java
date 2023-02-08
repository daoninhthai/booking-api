package com.daoninhthai.booking.repository;

import com.daoninhthai.booking.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long>, JpaSpecificationExecutor<ServiceEntity> {

    List<ServiceEntity> findByCategory(String category);

    List<ServiceEntity> findByActiveTrue();

    List<ServiceEntity> findByNameContainingIgnoreCase(String name);

    List<ServiceEntity> findByCategoryAndActiveTrue(String category);
}
