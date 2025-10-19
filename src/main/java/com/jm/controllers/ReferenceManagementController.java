package com.jm.controllers;

import com.jm.dto.AiPromptReferenceDTO;
import com.jm.dto.CityDTO;
import com.jm.dto.CountryDTO;
import com.jm.dto.EducationLevelDTO;
import com.jm.dto.ExerciseReferenceDTO;
import com.jm.dto.MealDTO;
import com.jm.dto.ProfessionDTO;
import com.jm.enums.AiProvider;
import com.jm.security.annotation.PermissionRequired;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/countries")
    public ResponseEntity<Page<CountryDTO>> listCountries(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(countryService.search(pageable, name, code, language));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/countries")
    public ResponseEntity<CountryDTO> createCountry(@RequestBody CountryDTO dto) {
        CountryDTO created = countryService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/countries/{id}")
    public ResponseEntity<CountryDTO> updateCountry(@PathVariable UUID id, @RequestBody CountryDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(countryService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/countries/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable UUID id) {
        countryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/cities")
    public ResponseEntity<Page<CityDTO>> listCities(Pageable pageable,
            @RequestParam(required = false) UUID countryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(cityService.search(pageable, countryId, name, stateCode, language));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/cities")
    public ResponseEntity<CityDTO> createCity(@RequestBody CityDTO dto) {
        CityDTO created = cityService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/cities/{id}")
    public ResponseEntity<CityDTO> updateCity(@PathVariable UUID id, @RequestBody CityDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(cityService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable UUID id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/education-levels")
    public ResponseEntity<Page<EducationLevelDTO>> listEducationLevels(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(educationLevelService.search(pageable, name, code, language));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/education-levels")
    public ResponseEntity<EducationLevelDTO> createEducationLevel(@RequestBody EducationLevelDTO dto) {
        EducationLevelDTO created = educationLevelService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/education-levels/{id}")
    public ResponseEntity<EducationLevelDTO> updateEducationLevel(@PathVariable UUID id,
            @RequestBody EducationLevelDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(educationLevelService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/education-levels/{id}")
    public ResponseEntity<Void> deleteEducationLevel(@PathVariable UUID id) {
        educationLevelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/professions")
    public ResponseEntity<Page<ProfessionDTO>> listProfessions(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(professionService.search(pageable, name, code, language));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/professions")
    public ResponseEntity<ProfessionDTO> createProfession(@RequestBody ProfessionDTO dto) {
        ProfessionDTO created = professionService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/professions/{id}")
    public ResponseEntity<ProfessionDTO> updateProfession(@PathVariable UUID id, @RequestBody ProfessionDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(professionService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/professions/{id}")
    public ResponseEntity<Void> deleteProfession(@PathVariable UUID id) {
        professionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/meals")
    public ResponseEntity<Page<MealDTO>> listMeals(Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(mealService.search(pageable, name, code, language));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/meals")
    public ResponseEntity<MealDTO> createMeal(@RequestBody MealDTO dto) {
        MealDTO created = mealService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/meals/{id}")
    public ResponseEntity<MealDTO> updateMeal(@PathVariable UUID id, @RequestBody MealDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(mealService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable UUID id) {
        mealService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/exercise-references")
    public ResponseEntity<Page<ExerciseReferenceDTO>> listExerciseReferences(Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String equipment) {
        return ResponseEntity.ok(
                exerciseReferenceService.search(pageable, search, language, muscleGroup, equipment));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/exercise-references")
    public ResponseEntity<ExerciseReferenceDTO> createExerciseReference(@RequestBody ExerciseReferenceDTO dto) {
        ExerciseReferenceDTO created = exerciseReferenceService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/exercise-references/{id}")
    public ResponseEntity<ExerciseReferenceDTO> updateExerciseReference(@PathVariable UUID id,
            @RequestBody ExerciseReferenceDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(exerciseReferenceService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/exercise-references/{id}")
    public ResponseEntity<Void> deleteExerciseReference(@PathVariable UUID id) {
        exerciseReferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_READ")
    @GetMapping("/ai-prompts")
    public ResponseEntity<Page<AiPromptReferenceDTO>> listAiPrompts(Pageable pageable,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) AiProvider provider,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String owner) {
        return ResponseEntity.ok(aiPromptReferenceService.search(pageable, code, name, model, provider, active, owner));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_CREATE")
    @PostMapping("/ai-prompts")
    public ResponseEntity<AiPromptReferenceDTO> createAiPrompt(@RequestBody AiPromptReferenceDTO dto) {
        AiPromptReferenceDTO created = aiPromptReferenceService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_UPDATE")
    @PutMapping("/ai-prompts/{id}")
    public ResponseEntity<AiPromptReferenceDTO> updateAiPrompt(@PathVariable UUID id,
            @RequestBody AiPromptReferenceDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(aiPromptReferenceService.save(dto));
    }

    @PermissionRequired("ROLE_REFERENCE_MANAGEMENT_DELETE")
    @DeleteMapping("/ai-prompts/{id}")
    public ResponseEntity<Void> deleteAiPrompt(@PathVariable UUID id) {
        aiPromptReferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
