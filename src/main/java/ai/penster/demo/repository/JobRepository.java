package ai.penster.demo.repository;

import ai.penster.demo.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // 1. Prevents duplicate scraping
    boolean existsByUrl(String url);

    // 2. Simple Search (title or company)
    List<Job> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(String title, String company);

    // 3. Simple Filter (location only)
    List<Job> findByLocationContainingIgnoreCase(String location);

    // 4. Combined Search AND Location Filter (Fixes your error)
    @Query("SELECT j FROM Job j WHERE " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Job> searchJobsWithLocation(@Param("keyword") String keyword, @Param("location") String location);
}
