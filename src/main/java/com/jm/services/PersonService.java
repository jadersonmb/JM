package com.jm.services;

import com.jm.dto.PersonDTO;
import com.jm.entity.Person;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.PersonMapper;
import com.jm.repository.PersonRepository;
import com.jm.speciation.PersonSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository repository;
    private final PersonMapper mapper;
    private final MessageSource messageSource;

    public PersonService(PersonRepository repository, PersonMapper mapper, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
    }

    public Page<PersonDTO> findAll(Pageable pageable, PersonDTO filter) throws JMException {
        return repository.findAll(PersonSpecification.search(filter), pageable).map(mapper::toDTO);
    }

    public PersonDTO createPerson(PersonDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    public PersonDTO findById(UUID id) throws JMException {
        ProblemType problemType = ProblemType.PERSON_NOT_FOUND;
        Optional<Person> obj = repository.findById(id);
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(),
                new Object[]{""}, LocaleContextHolder.getLocale());

        return mapper.toDTO(obj.orElseThrow(() ->
                new JMException(HttpStatus.BAD_REQUEST.value(),
                        problemType.getTitle(), problemType.getUri(), messageDetails)));
    }

    public PersonDTO updatePerson(PersonDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    public Person updatePersonEntity(Person entity) {
        return repository.save(entity);
    }
}

