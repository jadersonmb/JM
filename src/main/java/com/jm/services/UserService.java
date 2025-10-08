package com.jm.services;

import com.jm.dto.UserDTO;
import com.jm.entity.Country;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.UserMapper;
import com.jm.repository.CityRepository;
import com.jm.repository.CountryRepository;
import com.jm.repository.EducationLevelRepository;
import com.jm.repository.ProfessionRepository;
import com.jm.repository.UserRepository;
import com.jm.speciation.UserSpeciation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final UserMapper mapper;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;
    private final CountryRepository countriesRepository;
    private final CityRepository cityRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final ProfessionRepository professionRepository;

    public UserService(UserRepository repository, UserMapper mapper, MessageSource messageSource,
            PasswordEncoder passwordEncoder, CountryRepository countriesRepository, CityRepository cityRepository,
            EducationLevelRepository educationLevelRepository, ProfessionRepository professionRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
        this.passwordEncoder = passwordEncoder;
        this.countriesRepository = countriesRepository;
        this.cityRepository = cityRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.professionRepository = professionRepository;
    }

    public Page<UserDTO> findAll(Pageable pageable, UserDTO filter) throws JMException {
        return repository.findAll(UserSpeciation.search(filter), pageable).map(mapper::toDTO);
    }

    public UserDTO createUser(UserDTO dto) {
        Users entity = mapper.toEntity(dto);
        entity.setCountry(dto.getCountryId() != null ? countriesRepository.findById(dto.getCountryId()).orElse(null) : null);
        entity.setCity(dto.getCityId() != null ? cityRepository.findById(dto.getCityId()).orElse(null) : null);
        entity.setEducationLevel(dto.getEducationLevelId() != null
                ? educationLevelRepository.findById(dto.getEducationLevelId()).orElse(null)
                : null);
        entity.setProfession(dto.getProfessionId() != null
                ? professionRepository.findById(dto.getProfessionId()).orElse(null)
                : null);
        if (!StringUtils.hasText(entity.getPassword()) && dto.getId() != null) {
            repository.findById(dto.getId()).ifPresent(existing -> entity.setPassword(existing.getPassword()));
        }
        encodePasswordIfPresent(entity, dto.getPassword());
        if (entity.getType() == null) {
            entity.setType(Users.Type.CLIENT);
        }
        Users saved = repository.save(entity);
        logger.debug("User {} saved with id {}", saved.getEmail(), saved.getId());
        return mapper.toDTO(saved);
    }

    public Users findByEntityId(UUID id) throws JMException {
        return repository.findById(id).orElseThrow(this::userNotFound);
    }

    public UserDTO findById(UUID id) throws JMException {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::userNotFound));
    }

    public Users findEntityById(UUID id) throws JMException {
        return repository.findById(id).orElseThrow(this::userNotFound);
    }

    public UserDTO updateUser(UserDTO dto) {
        return createUser(dto);
    }

    public UserDTO updatePassword(UUID userId, String rawPassword) throws JMException {
        Users user = findEntityById(userId);
        encodePasswordIfPresent(user, rawPassword);
        Users updated = repository.save(user);
        return mapper.toDTO(updated);
    }

    public UserDTO updatePasswordByEmail(String email, String rawPassword) throws JMException {
        Users user = repository.findByEmail(email).orElseThrow(this::userNotFound);
        encodePasswordIfPresent(user, rawPassword);
        Users updated = repository.save(user);
        return mapper.toDTO(updated);
    }

    public Users getUserFromLabel(int hasCode) {
        return repository.findByHashCode(hasCode);
    }

    private JMException userNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private void encodePasswordIfPresent(Users entity, String rawPassword) {
        if (StringUtils.hasText(rawPassword)) {
            entity.setPassword(passwordEncoder.encode(rawPassword));
        }
    }
}