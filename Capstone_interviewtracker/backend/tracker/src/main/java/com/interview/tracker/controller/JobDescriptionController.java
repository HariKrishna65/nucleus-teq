package com.interview.tracker.controller;

import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.service.JobDescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jd")
@CrossOrigin("*")
public class JobDescriptionController {

    @Autowired
    private JobDescriptionService service;

    // ✅ CREATE JD (HR only)
    @PostMapping
    public ResponseEntity<?> createJD(@Valid @RequestBody JobDescription jd,
                                      @RequestParam String role) {

        if (!role.equals("HR")) {
            return ResponseEntity.status(403).body("Only HR can create JD");
        }

        return ResponseEntity.ok(service.save(jd));
    }

    // ✅ FETCH ALL
    @GetMapping
    public List<JobDescription> getAll() {
        return service.getAll();
    }

    // ✅ FETCH BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        JobDescription jd = service.getById(id);

        if (jd == null) {
            return ResponseEntity.status(404).body("JD not found");
        }

        return ResponseEntity.ok(jd);
    }

    // ✅ DELETE (HR only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                   @RequestParam String role) {

        if (!role.equals("HR")) {
            return ResponseEntity.status(403).body("Only HR can delete JD");
        }

        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}