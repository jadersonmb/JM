package com.jm.repository;

import com.jm.entity.Image;
import com.jm.entity.Users;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends CrudRepository<Image, UUID>, JpaSpecificationExecutor<Users> {

    Optional<List<Image>> findByUsersId(UUID userId);
    void deleteByFileName(String fileName);
}
