package com.example.specialistsearch;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // <-- New import
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
// import java.util.Locale; // <-- Removed unused import

/**
 * Main Spring Boot Application class.
 * This file contains the complete structure: Entity, Repository, Service, and Controller.
 */
@SpringBootApplication
public class SpecialistSearch implements CommandLineRunner {

    private final SpecialistRepository specialistRepository;

    // Dependency Injection via constructor
    public SpecialistSearch(SpecialistRepository specialistRepository) {
        this.specialistRepository = specialistRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpecialistSearch.class, args);
    }

    /**
     * Executes code immediately after the application starts up.
     * Used here to populate the H2 in-memory database with sample data.
     */
    @Override
    public void run(String... args) {
        System.out.println("Populating database with sample specialist data...");

        specialistRepository.save(new Specialist("Jane Smith", "Legal", "New York"));
        specialistRepository.save(new Specialist("Mark Lee", "Accounting", "Chicago"));
        specialistRepository.save(new Specialist("Sarah Chen", "Marketing", "New York"));
        specialistRepository.save(new Specialist("David Rodriguez", "Legal", "Miami"));
        specialistRepository.save(new Specialist("Anna Kowalski", "Accounting", "Chicago"));
        specialistRepository.save(new Specialist("Tom Harris", "Marketing", "Dallas"));
        specialistRepository.save(new Specialist("Ethan Hunt", "Legal", "Los Angeles"));
        specialistRepository.save(new Specialist("Peter Jones", "Accounting", "New York"));

        System.out.println("Sample data loaded successfully.");
        System.out.println("API is ready at: http://localhost:8080/api/specialists/search");
    }
}

// --- 1. JPA Entity ---

/**
 * Represents a Specialist in the database.
 * Uses Jakarta Persistence API (JPA) annotations.
 */
@Entity
class Specialist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty; // Legal, Accounting, Marketing

    @Column(nullable = false)
    private String city;

    // Default constructor for JPA
    public Specialist() {}

    // Parameterized constructor for easy creation
    public Specialist(String name, String specialty, String city) {
        this.name = name;
        this.specialty = specialty;
        this.city = city;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    @Override
    public String toString() {
        return "Specialist{" + "id=" + id + ", name='" + name + '\'' + ", specialty='" + specialty + '\'' + ", city='" + city + '\'' + '}';
    }
}

// --- 2. Spring Data JPA Repository ---

/**
 * Repository for data access operations.
 * Extends JpaRepository to get standard CRUD methods.
 */
@Repository
interface SpecialistRepository extends JpaRepository<Specialist, Long> {

    /**
     * Custom JPQL query to find specialists based on optional filters.
     *
     * @param specialty Optional filter for the specialist's area (e.g., "Legal").
     * @param text      Optional text for searching within the name OR city (case-insensitive).
     * @return List of matching Specialists.
     *
     * The logic ensures:
     * 1. If :specialty is null, the specialty check is ignored.
     * 2. If :text is null, the text search is ignored.
     * 3. If :text is present, it matches if it appears in the name OR the city.
     */
    @Query("SELECT s FROM Specialist s WHERE " +
            // 1. Specialty Filter (exact match, case sensitive is recommended for defined categories)
            "(:specialty IS NULL OR s.specialty = :specialty) AND " +

            // 2. Text Search Filter (case-insensitive check on name or city)
            "(:text IS NULL OR " +
            "LOWER(s.name) LIKE CONCAT('%', LOWER(:text), '%') OR " +
            "LOWER(s.city) LIKE CONCAT('%', LOWER(:text), '%'))")
    List<Specialist> findByFilters(@Param("specialty") String specialty, // <-- Uses imported @Param
                                   @Param("text") String text);          // <-- Uses imported @Param
}

// --- 3. Service Layer (Business Logic) ---

/**
 * Service class that handles the core business logic.
 */
@Service
class SpecialistService {

    private final SpecialistRepository specialistRepository;

    public SpecialistService(SpecialistRepository specialistRepository) {
        this.specialistRepository = specialistRepository;
    }

    /**
     * Searches for specialists using the provided optional filters.
     *
     * @param specialty The category filter (Legal, Accounting, Marketing). Nullable.
     * @param searchText The generic text input (Name or City). Nullable.
     * @return A list of matching specialists.
     */
    public List<Specialist> searchSpecialists(String specialty, String searchText) {
        // Normalize the specialty filter to handle case differences if necessary,
        // but keeping it as-is for exact match demonstration.
        // We ensure that empty strings are treated as null for the query simplicity.
        String filterSpecialty = (specialty != null && !specialty.trim().isEmpty()) ? specialty.trim() : null;
        String filterText = (searchText != null && !searchText.trim().isEmpty()) ? searchText.trim() : null;

        return specialistRepository.findByFilters(filterSpecialty, filterText);
    }
}


// --- 4. REST Controller ---

/**
 * REST Controller to expose the search endpoint.
 */
@RestController
@RequestMapping("/api/specialists")
class SpecialistController {

    private final SpecialistService specialistService;

    public SpecialistController(SpecialistService specialistService) {
        this.specialistService = specialistService;
    }

    /**
     * GET /api/specialists/search
     * Example URLs:
     * - http://localhost:8080/api/specialists/search
     * - http://localhost:8080/api/specialists/search?specialty=Legal
     * - http://localhost:8080/api/specialists/search?text=york
     * - http://localhost:8080/api/specialists/search?specialty=Accounting&text=Chicago
     */
    @GetMapping("/search")
    public List<Specialist> search(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String text) {

        return specialistService.searchSpecialists(specialty, text);
    }
}