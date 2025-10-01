package com.jm.services;

import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.UserMapper;
import com.jm.repository.UserRepository;
import com.jm.speciation.UserSpeciation;
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
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final UserMapper mapper;
    private final MessageSource messageSource;

    public UserService(UserRepository repository, UserMapper mapper, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
    }

    public Page<UserDTO> findAll(Pageable pageable, UserDTO filter) throws JMException {
        return repository.findAll(UserSpeciation.search(filter), pageable).map(mapper::toDTO);
    }

    public UserDTO createUser(UserDTO dto) {
        Users entity = mapper.toEntity(dto);
        entity.setType(Users.Type.CLIENT);
        return mapper.toDTO(repository.save(entity));
    }

    public Users findByEntityId(UUID id) throws JMException {
        ProblemType problemType = ProblemType.USER_NOT_EXISTS;
        Optional<Users> obj = repository.findById(id);
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[]{""}, LocaleContextHolder.getLocale());
        return obj.orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(),
                problemType.getTitle(), problemType.getUri(), messageDetails));
    }

    public UserDTO findById(UUID id) throws JMException {
        ProblemType problemType = ProblemType.USER_NOT_EXISTS;
        Optional<Users> obj = repository.findById(id);
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[]{""}, LocaleContextHolder.getLocale());
        return mapper
                .toDTO(obj.orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(),
                        problemType.getTitle(), problemType.getUri(), messageDetails)));
    }

    public Users findEntityById(UUID id) throws JMException {
        ProblemType problemType = ProblemType.USER_NOT_EXISTS;
        Optional<Users> obj = repository.findById(id);
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[]{""}, LocaleContextHolder.getLocale());
        return obj.orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(),
                problemType.getTitle(), problemType.getUri(), messageDetails));
    }

    public UserDTO updateUser(UserDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    public UserDTO updateUserEntity(Users entity) {
        return mapper.toDTO(repository.save(mapper.toUpdate(entity)));
    }

    public Users getUserFromLabel(int hasCode) {
        return repository.findByHashCode(hasCode);
    }
}
