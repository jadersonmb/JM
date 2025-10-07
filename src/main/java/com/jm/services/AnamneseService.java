package com.jm.services;

import com.jm.dto.AnamneseDTO;
import com.jm.entity.Anamnese;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.AnamneseMapper;
import com.jm.repository.AnamneseRepository;
import com.jm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

@Service
public class AnamneseService {

    private static final Logger logger = LoggerFactory.getLogger(AnamneseService.class);

    private final AnamneseRepository repository;
    private final AnamneseMapper mapper;
    private final MessageSource messageSource;
    private final UserRepository userRepository;

    public AnamneseService(AnamneseRepository repository, AnamneseMapper mapper, MessageSource messageSource,
            UserRepository userRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
        this.userRepository = userRepository;
    }

    public Page<AnamneseDTO> findAll(Pageable pageable, AnamneseDTO filter) {
        logger.debug("REST request to get all anamneses");

        Anamnese exampleEntity = mapper.toEntity(ObjectUtils.isEmpty(filter) ? new AnamneseDTO() : filter);
        exampleEntity.setId(null);
        exampleEntity.setExamesBioquimicos(null);
        exampleEntity.setRefeicoes24h(null);
        if (filter != null && filter.getUserId() != null) {
            Users user = new Users();
            user.setId(filter.getUserId());
            exampleEntity.setUser(user);
        }

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Anamnese> example = Example.of(exampleEntity, matcher);

        return repository.findAll(example, pageable).map(mapper::toDTO);
    }

    @Transactional
    public AnamneseDTO create(AnamneseDTO dto) {
        Users user = resolveUser(dto);
        Anamnese entity = mapper.toEntity(dto);
        entity.getExamesBioquimicos().forEach(exame -> exame.setAnamnese(entity));
        entity.getRefeicoes24h().forEach(refeicao -> refeicao.setAnamnese(entity));
        entity.setUser(user);
        applyPatientData(user, dto);

        Anamnese saved = repository.save(entity);
        userRepository.save(user);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("anamnese.saved", new Object[] { "" }, locale);
        logger.debug("{} - {}", message, saved.getId());
        return mapper.toDTO(saved);
    }

    public AnamneseDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::anamneseNotFound));
    }

    @Transactional
    public AnamneseDTO update(AnamneseDTO dto) {
        if (dto.getId() == null) {
            ProblemType problemType = ProblemType.INVALID_BODY;
            Locale locale = LocaleContextHolder.getLocale();
            String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                    locale);
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                    messageDetails);
        }

        repository.findById(dto.getId()).orElseThrow(this::anamneseNotFound);
        return create(dto);
    }

    @Transactional
    public void delete(UUID id) {
        Anamnese entity = repository.findById(id).orElseThrow(this::anamneseNotFound);
        repository.delete(entity);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("anamnese.deleted", new Object[] { "" }, locale);
        logger.debug("{} - {}", message, id);
    }

    private JMException anamneseNotFound() {
        ProblemType problemType = ProblemType.ANAMNESE_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private Users resolveUser(AnamneseDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            ProblemType problemType = ProblemType.INVALID_BODY;
            Locale locale = LocaleContextHolder.getLocale();
            String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                    locale);
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                    messageDetails);
        }
        return userRepository.findById(dto.getUserId()).orElseThrow(this::userNotFound);
    }

    private void applyPatientData(Users user, AnamneseDTO dto) {
        if (dto == null || user == null) {
            return;
        }
        if (StringUtils.hasText(dto.getPaciente())) {
            String[] parts = dto.getPaciente().trim().split(" ", 2);
            user.setName(parts[0]);
            if (parts.length > 1) {
                user.setLastName(parts[1]);
            }
        }
        if (StringUtils.hasText(dto.getEndereco())) {
            user.setStreet(dto.getEndereco());
        }
        if (StringUtils.hasText(dto.getTelefone())) {
            user.setPhoneNumber(dto.getTelefone());
        }
        user.setBirthDate(dto.getDataNascimento());
        user.setAge(dto.getIdade());
        user.setEducation(dto.getEscolaridade());
        user.setOccupation(dto.getProfissao());
        user.setConsultationGoal(dto.getObjetivoConsulta());
    }

    private JMException userNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }
}
