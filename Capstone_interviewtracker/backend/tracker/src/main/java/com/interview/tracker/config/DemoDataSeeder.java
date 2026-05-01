package com.interview.tracker.config;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.InterviewRound;
import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.JobDescriptionRepository;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PanelRepository panelRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository userRepository,
            PanelRepository panelRepository,
            JobDescriptionRepository jobDescriptionRepository,
            CandidateRepository candidateRepository,
            InterviewRepository interviewRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.panelRepository = panelRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.candidateRepository = candidateRepository;
        this.interviewRepository = interviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed panels (user + panel profile)
        Panel p1 = ensurePanel("Panel User", "panel1@test.com", "Java");
        Panel p2 = ensurePanel("Panel Two", "panel2@test.com", "Spring");

        // Seed a JD
        JobDescription jd = jobDescriptionRepository.findAll().stream().findFirst().orElseGet(() -> {
            JobDescription j = new JobDescription();
            j.setTitle("Java Developer");
            j.setDescription("Build and maintain Java applications using Spring Boot and REST APIs with good coding standards.");
            j.setSkills("Java, Spring");
            j.setExperienceMin(2);
            j.setExperienceMax(5);
            j.setSalaryMin(400000);
            j.setSalaryMax(900000);
            return jobDescriptionRepository.save(j);
        });

        // Seed a candidate user + candidate application
        User candidateUser = userRepository.findByEmail("candidate@test.com").orElseGet(() -> {
            User u = new User();
            u.setName("Candidate User");
            u.setEmail("candidate@test.com");
            u.setRole("CANDIDATE");
            u.setEmailVerified(true);
            u.setPassword(passwordEncoder.encode("Candidate@123"));
            return userRepository.save(u);
        });

        Candidate candidate = candidateRepository.findByUser_Id(candidateUser.getId()).stream().findFirst().orElseGet(() -> {
            Candidate c = new Candidate();
            c.setUser(candidateUser);
            c.setJd(jd);
            c.setPhone("9876543210");
            c.setExperience(3.0);
            c.setStatus("APPLIED");
            c.setApplicationDate(LocalDateTime.now().minusDays(1));
            c.setStage(Stage.SCREENING);
            c.setStageStatus(StageStatus.PENDING);
            return candidateRepository.save(c);
        });

        // Seed multiple interviews assigned to panels (so panel dashboard shows duplicates)
        // round values follow legacy DB constraint: L1/L2/HR
        ensureInterview(candidate, List.of(p1), InterviewRound.L1, "PENDING", LocalDateTime.now().plusDays(1));
        ensureInterview(candidate, List.of(p1), InterviewRound.L1, "PENDING", LocalDateTime.now().plusDays(2));
        ensureInterview(candidate, List.of(p1), InterviewRound.L2, "PENDING", LocalDateTime.now().plusDays(3));
        ensureInterview(candidate, List.of(p1), InterviewRound.HR, "COMPLETED", LocalDateTime.now().minusDays(2));

        ensureInterview(candidate, List.of(p2), InterviewRound.L2, "COMPLETED", LocalDateTime.now().minusDays(1));
        ensureInterview(candidate, List.of(p2), InterviewRound.L1, "PENDING", LocalDateTime.now().plusDays(4));
    }

    private Panel ensurePanel(String name, String email, String expertise) {
        userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setRole("PANEL");
            u.setEmailVerified(true);
            u.setPassword(passwordEncoder.encode("Panel@123"));
            return userRepository.save(u);
        });

        return panelRepository.findByEmail(email).orElseGet(() -> {
            Panel p = new Panel();
            p.setName(name);
            p.setEmail(email);
            p.setExpertise(expertise);
            return panelRepository.save(p);
        });
    }

    private void ensureInterview(Candidate candidate, List<Panel> panels, InterviewRound round, String status, LocalDateTime when) {
        boolean exists = interviewRepository.findAll().stream().anyMatch(i ->
                i.getCandidate() != null &&
                i.getCandidate().getId().equals(candidate.getId()) &&
                round.equals(i.getRound()) &&
                status.equals(i.getStatus()) &&
                i.getPanel() != null &&
                panels.get(0).getId().equals(i.getPanel().getId()) &&
                i.getInterviewTime() != null &&
                i.getInterviewTime().equals(when)
        );
        if (exists) return;

        Interview i = new Interview();
        i.setCandidate(candidate);
        i.setPanels(panels);
        i.setPanel(panels.get(0));
        i.setRound(round);
        i.setStatus(status);
        i.setFocusArea(candidate.getJd() != null && candidate.getJd().getSkills() != null ? candidate.getJd().getSkills() : "General");
        i.setInterviewTime(when);
        i.setInterviewerName(panels.get(0).getName() != null ? panels.get(0).getName() : panels.get(0).getEmail());
        interviewRepository.save(i);
    }
}

