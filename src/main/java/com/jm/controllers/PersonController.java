package com.jm.controllers;

import com.jm.dto.PersonDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.PersonService;
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

@RestController
@RequestMapping("/api/v1/persons")
@AllArgsConstructor
@Tag(name = "Persons", description = "Operations about persons")
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<?> createPerson(@RequestBody PersonDTO personDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(personDTO));
    }

    @GetMapping
    public ResponseEntity<?> listAll(Pageable pageable, PersonDTO filter) {
        logger.debug("REST request to get all persons");

        Page<PersonDTO> listAllPersons = personService.findAll(pageable, filter);
        return ResponseEntity.ok().body(listAllPersons);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody PersonDTO personDTO) {
        logger.debug("REST request to update Person : {}", personDTO);

        PersonDTO personSavedDTO = personService.findById(personDTO.getId());
        if (Objects.nonNull(personSavedDTO.getId())) {
            BeanUtils.copyProperties(personDTO, personSavedDTO, "id");
            personService.createPerson(personSavedDTO);
        }
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({JMException.class})
    public ResponseEntity<?> handleException(JMException ex) {
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