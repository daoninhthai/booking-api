package com.daoninhthai.booking.repository;

import com.daoninhthai.booking.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long>, JpaSpecificationExecutor<Provider> {

    Optional<Provider> findByUserId(Long userId);

    List<Provider> findByActiveTrue();

    @Query("SELECT p FROM Provider p JOIN p.services s WHERE s.id = :serviceId AND p.active = true")
    List<Provider> findByServiceId(@Param("serviceId") Long serviceId);

    List<Provider> findByBusinessNameContainingIgnoreCase(String businessName);

    @Query("SELECT p FROM Provider p WHERE p.address LIKE %:location% AND p.active = true")
    List<Provider> findByLocation(@Param("location") String location);
}
