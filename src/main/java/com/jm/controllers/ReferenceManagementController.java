package com.jm.controllers;

import com.jm.dto.AiPromptReferenceDTO;
import com.jm.dto.CityDTO;
import com.jm.dto.CountryDTO;
import com.jm.dto.EducationLevelDTO;
import com.jm.dto.ExerciseReferenceDTO;
import com.jm.dto.MealDTO;
import com.jm.dto.ProfessionDTO;
import com.jm.enums.AiProvider;
import com.jm.services.CityService;
import com.jm.services.CountryService;
import com.jm.services.EducationLevelService;
import com.jm.services.AiPromptReferenceService;
import com.jm.services.ExerciseReferenceService;
import com.jm.services.MealService;
import com.jm.services.ProfessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jm.utils.SecurityUtils;

@RestController
@RequestMapping("/api/v1/reference-management")
@RequiredArgsConstructor
@Tag(name = "Reference management", description = "Administrative CRUD for reference data")
public class ReferenceManagementController {

    private final CountryService countryService;
    private final CityService cityService;
    private final EducationLevelService educationLevelService;
    private final ProfessionService professionService;
    private final AiPromptReferenceService aiPromptReferenceService;
    private final MealService mealService;
    private final ExerciseReferenceService exerciseReferenceService;

    @GetMapping("/countries")
    public ResponseEntity<Page<CountryDTO>> listCountries(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        ensureAdmin();
        return ResponseEntity.ok(countryService.search(pageable, name, code, language));
    }

    @PostMapping("/countries")
    public ResponseEntity<CountryDTO> createCountry(@RequestBody CountryDTO dto) {
        ensureAdmin();
        CountryDTO created = countryService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/countries/{id}")
    public ResponseEntity<CountryDTO> updateCountry(@PathVariable UUID id, @RequestBody CountryDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(countryService.save(dto));
    }

    @DeleteMapping("/countries/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable UUID id) {
        ensureAdmin();
        countryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cities")
    public ResponseEntity<Page<CityDTO>> listCities(Pageable pageable,
            @RequestParam(required = false) UUID countryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String language) {
        ensureAdmin();
        return ResponseEntity.ok(cityService.search(pageable, countryId, name, stateCode, language));
    }

    @PostMapping("/cities")
    public ResponseEntity<CityDTO> createCity(@RequestBody CityDTO dto) {
        ensureAdmin();
        CityDTO created = cityService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/cities/{id}")
    public ResponseEntity<CityDTO> updateCity(@PathVariable UUID id, @RequestBody CityDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(cityService.save(dto));
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable UUID id) {
        ensureAdmin();
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/education-levels")
    public ResponseEntity<Page<EducationLevelDTO>> listEducationLevels(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        ensureAdmin();
        return ResponseEntity.ok(educationLevelService.search(pageable, name, code, language));
    }

    @PostMapping("/education-levels")
    public ResponseEntity<EducationLevelDTO> createEducationLevel(@RequestBody EducationLevelDTO dto) {
        ensureAdmin();
        EducationLevelDTO created = educationLevelService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/education-levels/{id}")
    public ResponseEntity<EducationLevelDTO> updateEducationLevel(@PathVariable UUID id,
            @RequestBody EducationLevelDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(educationLevelService.save(dto));
    }

    @DeleteMapping("/education-levels/{id}")
    public ResponseEntity<Void> deleteEducationLevel(@PathVariable UUID id) {
        ensureAdmin();
        educationLevelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professions")
    public ResponseEntity<Page<ProfessionDTO>> listProfessions(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        ensureAdmin();
        return ResponseEntity.ok(professionService.search(pageable, name, code, language));
    }

    @PostMapping("/professions")
    public ResponseEntity<ProfessionDTO> createProfession(@RequestBody ProfessionDTO dto) {
        ensureAdmin();
        ProfessionDTO created = professionService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/professions/{id}")
    public ResponseEntity<ProfessionDTO> updateProfession(@PathVariable UUID id, @RequestBody ProfessionDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(professionService.save(dto));
    }

    @DeleteMapping("/professions/{id}")
    public ResponseEntity<Void> deleteProfession(@PathVariable UUID id) {
        ensureAdmin();
        professionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meals")
    public ResponseEntity<Page<MealDTO>> listMeals(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        ensureAdmin();
        return ResponseEntity.ok(mealService.search(pageable, name, code, language));
    }

    @PostMapping("/meals")
    public ResponseEntity<MealDTO> createMeal(@RequestBody MealDTO dto) {
        ensureAdmin();
        MealDTO created = mealService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/meals/{id}")
    public ResponseEntity<MealDTO> updateMeal(@PathVariable UUID id, @RequestBody MealDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(mealService.save(dto));
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable UUID id) {
        ensureAdmin();
        mealService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exercise-references")
    public ResponseEntity<Page<ExerciseReferenceDTO>> listExerciseReferences(Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String equipment) {
        ensureAdmin();
        return ResponseEntity.ok(
                exerciseReferenceService.search(pageable, search, language, muscleGroup, equipment));
    }

    @PostMapping("/exercise-references")
    public ResponseEntity<ExerciseReferenceDTO> createExerciseReference(@RequestBody ExerciseReferenceDTO dto) {
        ensureAdmin();
        ExerciseReferenceDTO created = exerciseReferenceService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/exercise-references/{id}")
    public ResponseEntity<ExerciseReferenceDTO> updateExerciseReference(@PathVariable UUID id,
            @RequestBody ExerciseReferenceDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(exerciseReferenceService.save(dto));
    }

    @DeleteMapping("/exercise-references/{id}")
    public ResponseEntity<Void> deleteExerciseReference(@PathVariable UUID id) {
        ensureAdmin();
        exerciseReferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ai-prompts")
    public ResponseEntity<Page<AiPromptReferenceDTO>> listAiPrompts(Pageable pageable,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) AiProvider provider,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String owner) {
        ensureAdmin();
        return ResponseEntity.ok(aiPromptReferenceService.search(pageable, code, name, model, provider, active, owner));
    }

    @PostMapping("/ai-prompts")
    public ResponseEntity<AiPromptReferenceDTO> createAiPrompt(@RequestBody AiPromptReferenceDTO dto) {
        ensureAdmin();
        AiPromptReferenceDTO created = aiPromptReferenceService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/ai-prompts/{id}")
    public ResponseEntity<AiPromptReferenceDTO> updateAiPrompt(@PathVariable UUID id,
            @RequestBody AiPromptReferenceDTO dto) {
        ensureAdmin();
        dto.setId(id);
        return ResponseEntity.ok(aiPromptReferenceService.save(dto));
    }

    @DeleteMapping("/ai-prompts/{id}")
    public ResponseEntity<Void> deleteAiPrompt(@PathVariable UUID id) {
        ensureAdmin();
        aiPromptReferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void ensureAdmin() {
        if (!SecurityUtils.hasRole("ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
