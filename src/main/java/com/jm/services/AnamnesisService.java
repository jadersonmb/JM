package com.jm.services;

import com.jm.dto.AnamnesisDTO;
import com.jm.entity.Anamnesis;
import com.jm.entity.AnamnesisBiochemicalResult;
import com.jm.entity.AnamnesisFoodPreference;
import com.jm.entity.AnamnesisFoodRecall;
import com.jm.entity.AnamnesisFoodRecallItem;
import com.jm.entity.Pathology;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.AnamnesisMapper;
import com.jm.repository.AnamnesisRepository;
import com.jm.repository.CityRepository;
import com.jm.repository.CountryRepository;
import com.jm.repository.EducationLevelRepository;
import com.jm.repository.ProfessionRepository;
import com.jm.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AnamnesisService {

    private static final Logger logger = LoggerFactory.getLogger(AnamnesisService.class);

    private final AnamnesisRepository repository;
    private final AnamnesisMapper mapper;
    private final MessageSource messageSource;
    private final UserService userService;
    private final CityService cityService;
    private final EducationLevelService educationLevelService;
    private final ProfessionService professionService;
    private final FoodService foodService;
    private final CountryService countryService;
    private final PathologyService pathologyService;
    private final MeasurementUnitService measurementUnitService;
    private final BiochemicalExamService biochemicalExamService;

    public AnamnesisService(AnamnesisRepository repository, AnamnesisMapper mapper, MessageSource messageSource,
            UserService userService, CityService cityService, EducationLevelService educationLevelService,
            ProfessionService professionService, FoodService foodService, CountryService countryService,
            PathologyService pathologyService, MeasurementUnitService measurementUnitService,
            BiochemicalExamService biochemicalExamService) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
        this.userService = userService;
        this.cityService = cityService;
        this.educationLevelService = educationLevelService;
        this.professionService = professionService;
        this.foodService = foodService;
        this.countryService = countryService;
        this.pathologyService = pathologyService;
        this.measurementUnitService = measurementUnitService;
        this.biochemicalExamService = biochemicalExamService;
    }

    public Page<AnamnesisDTO> findAll(Pageable pageable, AnamnesisDTO filter) {
        logger.debug("REST request to get all anamneses");

        Anamnesis exampleEntity = mapper.toEntity(ObjectUtils.isEmpty(filter) ? new AnamnesisDTO() : filter);
        exampleEntity.setId(null);
        exampleEntity.setBiochemicalResults(null);
        exampleEntity.setFoodPreferences(null);
        exampleEntity.setFoodRecalls(null);
        exampleEntity.setPathologies(new HashSet<>());
        if (filter != null && filter.getUserId() != null) {
            Users user = new Users();
            user.setId(filter.getUserId());
            exampleEntity.setUser(user);
        }

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Anamnesis> example = Example.of(exampleEntity, matcher);

        return repository.findAll(example, pageable).map(mapper::toDTO);
    }

    @Transactional
    public AnamnesisDTO create(AnamnesisDTO dto) {
        Users user = resolveUser(dto);
        Anamnesis entity = mapper.toEntity(dto);
        attachManagedReferences(entity);
        entity.setUser(user);
        applyPatientData(user, dto);

        Anamnesis saved = repository.save(entity);
        userService.createUser(user);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("anamnesis.saved", new Object[] { "" }, locale);
        logger.debug("{} - {}", message, saved.getId());
        return mapper.toDTO(saved);
    }

    public AnamnesisDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::anamnesisNotFound));
    }

    @Transactional
    public AnamnesisDTO update(AnamnesisDTO dto) {
        if (dto.getId() == null) {
            ProblemType problemType = ProblemType.INVALID_BODY;
            Locale locale = LocaleContextHolder.getLocale();
            String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                    locale);
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                    messageDetails);
        }

        repository.findById(dto.getId()).orElseThrow(this::anamnesisNotFound);
        return create(dto);
    }

    @Transactional
    public void delete(UUID id) {
        Anamnesis entity = repository.findById(id).orElseThrow(this::anamnesisNotFound);
        repository.delete(entity);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("anamnesis.deleted", new Object[] { "" }, locale);
        logger.debug("{} - {}", message, id);
    }

    private void attachManagedReferences(Anamnesis entity) {
        bindPathologies(entity);
        bindBiochemicalResults(entity);
        bindFoodPreferences(entity);
        bindFoodRecalls(entity);
    }

    private void bindPathologies(Anamnesis entity) {
        Set<Pathology> managed = new HashSet<>();
        for (Pathology pathology : entity.getPathologies()) {
            if (pathology != null && pathology.getId() != null) {
                managed.add(pathologyService.findById(pathology.getId()));
            }
        }
        entity.setPathologies(managed);
    }

    private void bindBiochemicalResults(Anamnesis entity) {
        for (AnamnesisBiochemicalResult result : entity.getBiochemicalResults()) {
            if (result.getBiochemicalExam() != null && result.getBiochemicalExam().getId() != null) {
                result.setBiochemicalExam(biochemicalExamService.findByEntityId(result.getBiochemicalExam().getId()));
            } else {
                result.setBiochemicalExam(null);
            }
            result.setAnamnesis(entity);
        }
    }

    private void bindFoodPreferences(Anamnesis entity) {
        for (AnamnesisFoodPreference preference : entity.getFoodPreferences()) {
            if (preference.getFood() != null && preference.getFood().getId() != null) {
                preference.setFood(foodService.findByEntityId(preference.getFood().getId()));
            } else {
                preference.setFood(null);
            }
            preference.setAnamnesis(entity);
        }
    }

    private void bindFoodRecalls(Anamnesis entity) {
        for (AnamnesisFoodRecall recall : entity.getFoodRecalls()) {
            recall.setAnamnesis(entity);
            for (AnamnesisFoodRecallItem item : recall.getItems()) {
                if (item.getFood() != null && item.getFood().getId() != null) {
                    item.setFood(foodService.findByEntityId(item.getFood().getId()));
                } else {
                    item.setFood(null);
                }
                if (item.getMeasurementUnit() != null && item.getMeasurementUnit().getId() != null) {
                    item.setMeasurementUnit(measurementUnitService.findByEntityId(item.getMeasurementUnit().getId()));
                } else {
                    item.setMeasurementUnit(null);
                }
                item.setFoodRecall(recall);
            }
        }
    }

    private JMException anamnesisNotFound() {
        ProblemType problemType = ProblemType.ANAMNESIS_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }

    private Users resolveUser(AnamnesisDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            ProblemType problemType = ProblemType.INVALID_BODY;
            Locale locale = LocaleContextHolder.getLocale();
            String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                    locale);
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                    messageDetails);
        }
        return userService.findEntityById(dto.getUserId());
    }

    private void applyPatientData(Users user, AnamnesisDTO dto) {
        if (dto == null || user == null) {
            return;
        }
        if (StringUtils.hasText(dto.getPatientName())) {
            String[] parts = dto.getPatientName().trim().split(" ", 2);
            user.setName(parts[0]);
            if (parts.length > 1) {
                user.setLastName(parts[1]);
            }
        }
        if (StringUtils.hasText(dto.getAddress())) {
            user.setStreet(dto.getAddress());
        }
        if (StringUtils.hasText(dto.getPhoneNumber())) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        user.setBirthDate(dto.getBirthDate());
        user.setAge(dto.getAge());
        user.setConsultationGoal(dto.getConsultationGoal());
        if (dto.getCountryId() != null) {
            user.setCountry(countryService.findEntityById(dto.getCountryId()));
        } else {
            user.setCountry(null);
        }
        if (dto.getCityId() != null) {
            user.setCity(cityService.findEntityById(dto.getCityId()));
        } else {
            user.setCity(null);
        }
        if (dto.getEducationLevelId() != null) {
            user.setEducationLevel(educationLevelService.findEntityById(dto.getEducationLevelId()));
        } else {
            user.setEducationLevel(null);
        }
        if (dto.getProfessionId() != null) {
            user.setProfession(professionService.findEntityById(dto.getProfessionId()));
        } else {
            user.setProfession(null);
        }
    }

    private JMException userNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }
}
