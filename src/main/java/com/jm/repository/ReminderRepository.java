package com.jm.repository;

import com.jm.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID>, JpaSpecificationExecutor<Reminder> {

    @Query("select r from Reminder r where r.active = true and r.completed = false and r.scheduledAt <= :now "
            + "and (r.dispatchedAt is null or r.dispatchedAt < r.scheduledAt)")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);
}
