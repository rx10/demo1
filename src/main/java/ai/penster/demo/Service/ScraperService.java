package ai.penster.demo.Service;

import ai.penster.demo.entity.Job;
import ai.penster.demo.repository.JobRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class ScraperService {

    private final JobRepository jobRepository;
    private static final String REAL_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    public ScraperService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void scrapeAllPlatforms(String title, String location) {
        // Inside scrapeAllPlatforms
BrowserContext context = browser.newContext(new Browser.NewContextOptions()
    .setUserAgent(REAL_UA)
    .setViewportSize(1920, 1080)
    .setDeviceScaleFactor(1)
    .setHasTouch(false)
    .setLanguages(java.util.List.of("en-US", "en"))); 

// CRITICAL: Execute a script to hide the 'webdriver' flag
context.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        System.out.println("Starting Scraper...");
        jobRepository.deleteAll();

        // 1. Initialize Playwright with Arch-friendly settings
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false) 
                    .setSlowMo(100)); // Crucial for bypassing detection on fallback builds

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(REAL_UA)
                    .setViewportSize(1920, 1080));

            // 2. Execute platform-specific scrapes
            scrapeDice(context, title, location, 20);

            browser.close();
        } catch (Exception e) {
            System.err.println("Global Scraper Error: " + e.getMessage());
        }
    }

private void scrapeDice(BrowserContext context, String title, String location, int targetCount) {
    Page page = context.newPage();
    try {
        System.out.println("Navigating to Dice...");
        page.navigate("https://www.dice.com/jobs");

        // --- THE SECURITY CHECK ---
        // If the search bar isn't visible within 5 seconds, 
        // it's almost certainly a security challenge.
        try {
            page.waitForSelector("input#typeaheadInput", new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            System.out.println("⚠️ ACTION REQUIRED: Solving security challenge in the browser window...");
            // Now wait 60 seconds to give YOU time to click the 'I am human' box
            page.waitForSelector("input#typeaheadInput", new Page.WaitForSelectorOptions().setTimeout(60000));
            System.out.println("✅ Challenge bypassed! Continuing scrape...");
        }
        // ---------------------------

        System.out.println("Filling search criteria for: " + title);
        page.fill("input#typeaheadInput", title);
        
        // Use type for the location to be "stealthier"
        page.locator("input#google-location-search").focus();
        page.keyboard().type(location);
        page.keyboard().press("Enter");

        // Wait for results to load
        page.waitForSelector("dhi-search-card", new Page.WaitForSelectorOptions().setTimeout(30000));

        // ... extraction logic ...

    } catch (Exception e) {
        System.err.println("Dice Scrape failed: " + e.getMessage());
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("dice-debug.png")));
    } finally {
        page.close();
    }
}
    private void extractAndSaveJob(Locator card) {
        try {
            Job job = new Job();
            job.setTitle(card.locator(".card-title-link").innerText().trim());
            job.setCompany(card.locator(".card-company a").innerText().trim());
            job.setLocation(card.locator(".card-location").innerText().trim());
            job.setUrl(card.locator(".card-title-link").getAttribute("href"));

            if (job.getUrl() != null && !jobRepository.existsByUrl(job.getUrl())) {
                jobRepository.save(job);
                System.out.println("Saved: " + job.getTitle());
            }
        } catch (Exception e) {
            // Individual card failure shouldn't stop the whole scrape
            System.err.println("Failed to parse card: " + e.getMessage());
        }
    }
}