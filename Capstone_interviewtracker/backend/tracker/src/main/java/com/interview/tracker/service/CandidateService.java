package com.interview.tracker.service;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

   
    public Candidate createCandidate(Candidate candidate, MultipartFile file) throws IOException {

        
        if (candidate.getUser() != null &&
                userRepository.findByEmail(candidate.getUser().getEmail()).isPresent()) {
            throw new RuntimeException("Candidate with this email already exists");
        }

        
        if (file != null && !file.isEmpty()) {

            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filePath = UPLOAD_DIR + fileName;

            file.transferTo(new File(filePath));

            candidate.setResumeUrl(filePath);
        }

        return candidateRepository.save(candidate);
    }

   
    public List<Candidate> getByUser(Long userId) {
        return candidateRepository.findByUser_Id(userId);
    }
}