package com.translator.repository;

import com.translator.entity.AnalyzedTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyzedTicketRepository extends JpaRepository<AnalyzedTicketEntity, Long> {

    List<AnalyzedTicketEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT t FROM AnalyzedTicketEntity t WHERE " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(t.passengerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.pnrNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.carrierNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.departureStation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.arrivalDestination) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.fileName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY t.createdAt DESC")
    Page<AnalyzedTicketEntity> searchTickets(@Param("query") String query, Pageable pageable);

    @Query("SELECT t FROM AnalyzedTicketEntity t WHERE " +
            "(:transportType IS NULL OR :transportType = '' OR LOWER(t.transportType) = LOWER(:transportType)) AND " +
            "(:langCode IS NULL OR :langCode = '' OR LOWER(t.detectedLanguageCode) = LOWER(:langCode)) " +
            "ORDER BY t.createdAt DESC")
    List<AnalyzedTicketEntity> findFiltered(
            @Param("transportType") String transportType,
            @Param("langCode") String langCode
    );
}
