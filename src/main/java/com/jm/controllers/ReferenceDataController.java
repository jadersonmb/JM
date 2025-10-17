package com.jm.controllers;

import com.jm.dto.BiochemicalExamDTO;
import com.jm.dto.CityDTO;
import com.jm.dto.CountryDTO;
import com.jm.dto.EducationLevelDTO;
import com.jm.dto.ExerciseReferenceDTO;
import com.jm.dto.FoodCategoryDTO;
import com.jm.dto.FoodDTO;
import com.jm.dto.MealDTO;
import com.jm.dto.MeasurementUnitDTO;
import com.jm.dto.PathologyDTO;
import com.jm.dto.ProfessionDTO;
import com.jm.services.BiochemicalExamService;
import com.jm.services.CityService;
import com.jm.services.CountryService;
import com.jm.services.EducationLevelService;
import com.jm.services.ExerciseReferenceService;
import com.jm.services.FoodCategoryService;
import com.jm.services.FoodService;
import com.jm.services.MealService;
import com.jm.services.MeasurementUnitService;
import com.jm.services.PathologyService;
import com.jm.services.ProfessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reference")
@Tag(name = "Reference data", description = "Lookup tables for nutrition and profile data")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final CountryService countryService;
    private final CityService cityService;
    private final EducationLevelService educationLevelService;
    private final ProfessionService professionService;
    private final MeasurementUnitService measurementUnitService;
    private final FoodCategoryService foodCategoryService;
    private final FoodService foodService;
    private final MealService mealService;
    private final PathologyService pathologyService;
    private final BiochemicalExamService biochemicalExamService;
    private final ExerciseReferenceService exerciseReferenceService;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryDTO>> listCountries(@RequestParam(required = false) String language) {
        return ResponseEntity.ok(countryService.findAll(language));
    }

    @GetMapping("/countries/{countryId}/cities")
    public ResponseEntity<List<CityDTO>> listCities(@PathVariable UUID countryId,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(cityService.findByCountry(countryId, language));
    }

    @GetMapping("/education-levels")
    public ResponseEntity<List<EducationLevelDTO>> listEducationLevels(
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(educationLevelService.findAll(language));
    }

    @GetMapping("/professions")
    public ResponseEntity<List<ProfessionDTO>> listProfessions(@RequestParam(required = false) String language) {
        return ResponseEntity.ok(professionService.findAll(language));
    }

    @GetMapping("/measurement-units")
    public ResponseEntity<List<MeasurementUnitDTO>> listMeasurementUnits(
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(measurementUnitService.findAll(language));
    }

    @GetMapping("/food-categories")
    public ResponseEntity<List<FoodCategoryDTO>> listFoodCategories(
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(foodCategoryService.findAll(language));
    }

    @GetMapping("/foods")
    public ResponseEntity<List<FoodDTO>> listFoods(@RequestParam(required = false) String language,
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(foodService.findAll(language, categoryId));
    }

    @GetMapping("/meals")
    public ResponseEntity<List<MealDTO>> listMeals(@RequestParam(required = false) String language) {
        return ResponseEntity.ok(mealService.findAll(language));
    }

    @GetMapping("/pathologies")
    public ResponseEntity<List<PathologyDTO>> listPathologies(@RequestParam(required = false) String language) {
        return ResponseEntity.ok(pathologyService.findAll(language));
    }

    @GetMapping("/biochemical-exams")
    public ResponseEntity<List<BiochemicalExamDTO>> listBiochemicalExams(
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(biochemicalExamService.findAll(language));
    }

    @GetMapping("/exercise-references")
    public ResponseEntity<List<ExerciseReferenceDTO>> listExerciseReferences(
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(exerciseReferenceService.findAll(language));
    }
}
