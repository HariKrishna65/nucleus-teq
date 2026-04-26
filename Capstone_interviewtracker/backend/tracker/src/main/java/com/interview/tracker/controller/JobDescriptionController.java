package com.interview.tracker.controller;

import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.service.JobDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jd")
public class JobDescriptionController {

    @Autowired
    private JobDescriptionService service;

    // ✅ CREATE JD (HR only)
    @PostMapping
    public ResponseEntity<?> createJD(@RequestBody JobDescription jd,
                                      @RequestParam String role) {

        if (!role.equals("HR")) {
            return ResponseEntity.status(403).body("Only HR can create JD");
        }

        return ResponseEntity.ok(service.save(jd));
    }

    // ✅ GET ALL JD
    @GetMapping
    public List<JobDescription> getAll() {
        return service.getAll();
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public JobDescription getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ✅ DELETE JD
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam String role) {

        if (!role.equals("HR")) {
            return "Only HR can delete JD";
        }

        service.delete(id);
        return "Deleted successfully";
    }
}