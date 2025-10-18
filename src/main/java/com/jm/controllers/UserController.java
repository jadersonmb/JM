package com.jm.controllers;

import com.jm.dto.UserDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "Users", description = "Operations about users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }

    @GetMapping
    public ResponseEntity<?> listAll(Pageable pageable, UserDTO filter) {
        logger.debug("REST request to get all users");

        Page<UserDTO> listAllAccountDTO = userService.findAll(pageable, filter);
        return ResponseEntity.ok().body(listAllAccountDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable UUID id) throws JMException {
        logger.debug("REST request to get user {}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping
    public ResponseEntity<UserDTO> update(@RequestBody UserDTO userDTO) {
        logger.debug("REST request to update User : {}", userDTO);

        UserDTO userSaveDTO = userService.findById(userDTO.getId());
        if (Objects.nonNull(userSaveDTO.getId())) {
            BeanUtils.copyProperties(userDTO, userSaveDTO, "id");
            UserDTO updated = userService.createUser(userSaveDTO);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.ok(userSaveDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete User : {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recovery-password")
    public ResponseEntity<Void> recoveryPassword(@RequestParam String email) throws JMException {
        logger.debug("REST request to initiate password recovery for {}", email);
        userService.initiatePasswordRecovery(email);
        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler({ JMException.class })
    public ResponseEntity<?> Exception(JMException ex) {
        Problem problem = createProblemBuild(ex.getStatus(), ex.getDetails(), ex.getType(), ex.getTitle())
                .build();
        return ResponseEntity.badRequest().body(problem);
    }

    private Problem.ProblemBuilder createProblemBuild(Integer status, String detail, String type, String title) {
        return Problem.builder()
                .status(status)
                .details(detail)
                .type(type)
                .title(title);
    }
}
