package ai.penster.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data // Lombok annotation: auto-generates getters, setters, equals, and hashCode
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;
    private String postedDate;
    private String salary;

    // Requirement: extract first 200 characters
    @Column(length = 255)
    private String descriptionSnippet;

    // Requirement: direct link & prevent duplicates
    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String url;

    // Requirement: track when scraped
    private LocalDateTime scrapedAt;

    // This automatically sets the timestamp right before saving to the database
    @PrePersist
    protected void onCreate() {
        scrapedAt = LocalDateTime.now();
    }
}
