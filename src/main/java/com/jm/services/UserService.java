package com.jm.services;

import com.jm.configuration.config.EmailProperties;
import com.jm.dto.ChangePasswordDTO;
import com.jm.dto.RoleDTO;
import com.jm.dto.UserDTO;
import com.jm.services.email.EmailNotificationService;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.UserMapper;
import com.jm.repository.CityRepository;
import com.jm.repository.CountryRepository;
import com.jm.repository.EducationLevelRepository;
import com.jm.repository.ProfessionRepository;
import com.jm.repository.UserRepository;
import com.jm.repository.RoleRepository;
import com.jm.entity.Role;
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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private static final int PASSWORD_LENGTH = 12;
    private static final int PASSWORD_RECOVERY_EXPIRATION_MINUTES = 30;
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;
    private final CountryRepository countriesRepository;
    private final CityRepository cityRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final ProfessionRepository professionRepository;
    private final EmailNotificationService emailNotificationService;
    private final EmailProperties emailProperties;

    public UserService(UserRepository repository, UserMapper mapper, MessageSource messageSource,
            PasswordEncoder passwordEncoder, CountryRepository countriesRepository, CityRepository cityRepository,
            EducationLevelRepository educationLevelRepository, ProfessionRepository professionRepository,
            EmailNotificationService emailNotificationService, EmailProperties emailProperties, RoleRepository roleRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
        this.passwordEncoder = passwordEncoder;
        this.countriesRepository = countriesRepository;
        this.cityRepository = cityRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.professionRepository = professionRepository;
        this.emailNotificationService = emailNotificationService;
        this.emailProperties = emailProperties;
        this.roleRepository = roleRepository;
    }

    public Page<UserDTO> findAll(Pageable pageable, UserDTO filter) throws JMException {
        return repository.findAll(UserSpeciation.search(filter), pageable).map(mapper::toDTO);
    }

    public void createUser(Users user) {
        boolean isNew = user.getId() == null;
        if (isNew && user.getFirstAccess() == null) {
            user.setFirstAccess(Boolean.TRUE);
        }
        ensureDefaultRole(user);
        Users saved = repository.save(user);
        if (isNew) {
            emailNotificationService.sendUserConfirmation(saved);
        }
    }

    public UserDTO createUser(UserDTO dto) {
        Users entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElseThrow(this::userNotFound);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
        }

        applyReferenceData(entity, dto);

        boolean isNewUser = entity.getId() == null;
        String temporaryPassword = null;
        if (isNewUser) {
            temporaryPassword = generateTemporaryPassword();
            entity.setPassword(passwordEncoder.encode(temporaryPassword));
            entity.setFirstAccess(Boolean.TRUE);
        } else {
            encodePasswordIfPresent(entity, dto.getPassword());
        }

        if (entity.getType() == null) {
            entity.setType(Users.Type.CLIENT);
        }

        applyRoles(entity, dto.getRoles());
        ensureDefaultRole(entity);
        Users saved = repository.save(entity);

        if (isNewUser) {
            UserDTO welcomeDto = mapper.toDTO(saved);
            welcomeDto.setPassword(temporaryPassword);
            welcomeDto.setLocale(dto.getLocale());
            emailNotificationService.sendWelcomeEmail(welcomeDto);
            emailNotificationService.sendUserConfirmation(saved);
        }

        logger.debug("User {} saved with id {}", saved.getEmail(), saved.getId());
        UserDTO result = mapper.toDTO(saved);
        result.setPassword(null);
        return result;
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

    public List<UserDTO> findAllWithRoles() {
        return repository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public UserDTO updateUserRoles(UUID userId, Set<UUID> roleIds) {
        Users user = repository.findById(userId).orElseThrow(this::userNotFound);
        Set<Role> roles = resolveRoles(roleIds);
        user.setRoles(roles);
        ensureDefaultRole(user);
        Users saved = repository.save(user);
        UserDTO dto = mapper.toDTO(saved);
        dto.setPassword(null);
        return dto;
    }

    public UserDTO updateUser(UserDTO dto) {
        return createUser(dto);
    }

    public UserDTO updatePassword(UUID userId, String rawPassword) throws JMException {
        Users user = findEntityById(userId);
        encodePasswordIfPresent(user, rawPassword);
        user.setFirstAccess(Boolean.FALSE);
        Users updated = repository.save(user);
        return mapper.toDTO(updated);
    }

    public UserDTO updatePasswordByEmail(String email, String rawPassword) throws JMException {
        Users user = repository.findByEmail(email).orElseThrow(this::userNotFound);
        encodePasswordIfPresent(user, rawPassword);
        user.setFirstAccess(Boolean.FALSE);
        Users updated = repository.save(user);
        return mapper.toDTO(updated);
    }

    public void initiatePasswordRecovery(String email) throws JMException {
        Users user = repository.findByEmail(email).orElseThrow(this::userNotFound);
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(PASSWORD_RECOVERY_EXPIRATION_MINUTES);
        user.setPasswordRecoveryToken(token);
        user.setPasswordRecoveryTokenExpiresAt(expiresAt);
        Users saved = repository.save(user);
        UserDTO dto = mapper.toDTO(saved);
        dto.setLocale(user.getLocale());
        dto.setPassword(null);
        emailNotificationService.sendPasswordRecoveryEmail(dto, buildPasswordRecoveryUrl(token));
    }

    public UserDTO resetPasswordWithToken(String token, String rawPassword) throws JMException {
        if (!StringUtils.hasText(token)) {
            throw invalidToken();
        }
        Users user = repository.findByPasswordRecoveryToken(token).orElseThrow(this::invalidToken);
        if (user.getPasswordRecoveryTokenExpiresAt() == null
                || user.getPasswordRecoveryTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw expiredToken();
        }
        encodePasswordIfPresent(user, rawPassword);
        user.setPasswordRecoveryToken(null);
        user.setPasswordRecoveryTokenExpiresAt(null);
        user.setFirstAccess(Boolean.FALSE);
        Users saved = repository.save(user);
        UserDTO dto = mapper.toDTO(saved);
        dto.setPassword(null);
        return dto;
    }

    public void changePasswordFirstAccess(ChangePasswordDTO dto) {
        Users user = repository.findById(dto.getUserId()).orElseThrow(this::userNotFound);

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw invalidPassword();
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setFirstAccess(Boolean.FALSE);
        repository.save(user);
    }

    public Users getUserFromLabel(int hasCode) {
        return repository.findByHashCode(hasCode);
    }

    private void applyRoles(Users entity, Collection<RoleDTO> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        Set<UUID> ids = roles.stream()
                .map(RoleDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!ids.isEmpty()) {
            entity.setRoles(resolveRoles(ids));
        }
    }

    private Set<Role> resolveRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Role> roles = new HashSet<>();
        for (UUID id : roleIds) {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new JMException(HttpStatus.NOT_FOUND.value(),
                            ProblemType.INVALID_DATA.getUri(),
                            ProblemType.INVALID_DATA.getTitle(),
                            messageSource.getMessage("roles.not-found", new Object[] { id }, LocaleContextHolder.getLocale())));
            roles.add(role);
        }
        return roles;
    }

    private void ensureDefaultRole(Users user) {
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            return;
        }
        roleRepository.findByNameIgnoreCase("CLIENT").ifPresent(role -> user.getRoles().add(role));
    }

    private JMException userNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private JMException invalidToken() {
        ProblemType problemType = ProblemType.INVALID_TOKEN;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), null,
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private JMException expiredToken() {
        ProblemType problemType = ProblemType.EXPIRED_TOKEN;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), null,
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private JMException invalidPassword() {
        ProblemType problemType = ProblemType.INVALID_PASSWORD;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), null,
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private void encodePasswordIfPresent(Users entity, String rawPassword) {
        if (StringUtils.hasText(rawPassword)) {
            entity.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    private String buildPasswordRecoveryUrl(String token) {
        String base = emailProperties.getPasswordRecoveryBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = "https://app.nutrivision.ai/reset-password";
        }
        if (base.contains("?")) {
            if (base.endsWith("?")) {
                return base + "token=" + token;
            }
            return base + "&token=" + token;
        }
        return base + "?token=" + token;
    }

    private String generateTemporaryPassword() {
        StringBuilder builder = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = PASSWORD_RANDOM.nextInt(PASSWORD_ALPHABET.length());
            builder.append(PASSWORD_ALPHABET.charAt(index));
        }
        return builder.toString();
    }

    private void applyReferenceData(Users entity, UserDTO dto) {
        entity.setCountry(dto.getCountryId() != null
                ? countriesRepository.findById(dto.getCountryId()).orElse(null)
                : null);
        entity.setCity(dto.getCityId() != null ? cityRepository.findById(dto.getCityId()).orElse(null) : null);
        entity.setEducationLevel(dto.getEducationLevelId() != null
                ? educationLevelRepository.findById(dto.getEducationLevelId()).orElse(null)
                : null);
        entity.setProfession(dto.getProfessionId() != null
                ? professionRepository.findById(dto.getProfessionId()).orElse(null)
                : null);
    }

    public void delete(UUID id) {
        Users entity = repository.findById(id).orElseThrow(this::userNotFound);
        repository.delete(entity);
    }
}