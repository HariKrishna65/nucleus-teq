package com.interview.tracker.controller;

import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.service.JobDescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.interview.tracker.constants.AppConstants.JD;

@RestController
@RequestMapping(JD)
public class JobDescriptionController {

    private final JobDescriptionService service;

    public JobDescriptionController(JobDescriptionService service) {
        this.service = service;
    }

    
    @PostMapping
    public ResponseEntity<?> createJD(@Valid @RequestBody JobDescription jd) {

        return ResponseEntity.ok(service.save(jd));
    }

    
    @GetMapping
    public List<JobDescription> getAll() {
        return service.getAll();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        JobDescription jd = service.getById(id);

        if (jd == null) {
            return ResponseEntity.status(404).body("JD not found");
        }

        return ResponseEntity.ok(jd);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}