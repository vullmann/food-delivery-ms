package de.ullmann.fooddelivery.common.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * get all not processed events sorted by creation time
     * used by Outbox-Publisher
     */
    @Query("""
            SELECT o FROM OutboxEvent o 
            WHERE o.processedAt IS NULL 
            ORDER BY o.createdAt ASC
            """)
    List<OutboxEvent> findUnprocessedEvents(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    @Query("""
            SELECT o FROM OutboxEvent o 
            WHERE o.processedAt IS NULL 
            ORDER BY o.createdAt ASC
            LIMIT :limit
            """)
    List<OutboxEvent> findTopUnprocessedEvents(@Param("limit") int limit);

    /**
     * counts not processed events
     */
    long countByProcessedAtIsNull();

    /**
     * find an event by aggregateId and processedAt is null (not processed yet)
     */
    List<OutboxEvent> findByAggregateIdAndProcessedAtIsNull(UUID aggregateId);
}