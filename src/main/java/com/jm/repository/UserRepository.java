package com.jm.repository;

import com.jm.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID>, JpaSpecificationExecutor<Users> {

    Users findByHashCode(int hasCode);

    Optional<Users> findByEmail(String username);

    Optional<Users> findByPhoneNumber(String phoneNumber);

    List<Users> findTop50ByTypeOrderByNameAsc(Users.Type type);

    List<Users> findTop20ByTypeAndNameContainingIgnoreCaseOrderByNameAsc(Users.Type type, String name);

    List<Users> findTop20ByTypeAndEmailContainingIgnoreCaseOrderByEmailAsc(Users.Type type, String email);
}
