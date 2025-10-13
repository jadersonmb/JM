package com.jm.repository;

import com.jm.entity.PhotoEvolution;
import com.jm.enums.BodyPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoEvolutionRepository extends JpaRepository<PhotoEvolution, UUID> {

    List<PhotoEvolution> findByUser_IdOrderByCapturedAtDesc(UUID userId);

    List<PhotoEvolution> findByUser_IdAndBodyPartOrderByCapturedAtDesc(UUID userId, BodyPart bodyPart);

    List<PhotoEvolution> findByBodyPartOrderByCapturedAtDesc(BodyPart bodyPart);

    List<PhotoEvolution> findAllByOrderByCapturedAtDesc();
}
