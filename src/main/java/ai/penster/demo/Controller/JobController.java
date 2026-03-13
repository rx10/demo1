package ai.penster.demo.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ai.penster.demo.Service.ScraperService;
import ai.penster.demo.entity.Job;
import ai.penster.demo.repository.JobRepository;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*") // Allows your Next.js frontend to call this API
public class JobController {

    private final ScraperService scraperService;
    private final JobRepository jobRepository; // Added the repository

    // Constructor injection for both services
    public JobController(ScraperService scraperService, JobRepository jobRepository) {
        this.scraperService = scraperService;
        this.jobRepository = jobRepository;
    }

    /**
     * GET /api/jobs
     * Retrieves jobs. Supports optional search (title/company) and location filters.
     */
    @GetMapping
    public List<Job> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location) {

        // Both search and location are provided
        if (search != null && !search.isEmpty() && location != null && !location.isEmpty()) {
            return jobRepository.searchJobsWithLocation(search, location);
        }
        
        // Only search (title/company) is provided
        if (search != null && !search.isEmpty()) {
            return jobRepository.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(search, search);
        }
        
        // Only location is provided
        if (location != null && !location.isEmpty()) {
            return jobRepository.findByLocationContainingIgnoreCase(location);
        }

        // No filters applied, return all jobs
        return jobRepository.findAll();
    }

    /**
     * POST /api/jobs/scrape
     * Triggers the background scraping process.
     */
    @PostMapping("/scrape")
    public ResponseEntity<String> triggerScrape(@RequestBody Map<String, String> payload) {
        String jobTitle = payload.getOrDefault("title", "Software Developer");
        String location = payload.getOrDefault("location", "California");

        // Run in a new thread so the frontend doesn't timeout while waiting for 50 jobs to scrape
        new Thread(() -> scraperService.scrapeAllPlatforms(jobTitle, location)).start();

        return ResponseEntity.ok("Scraping started for: " + jobTitle + " in " + location);
    }
}
