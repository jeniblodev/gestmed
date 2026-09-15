package com.gestmed.scheduling.config;

import com.gestmed.scheduling.entity.Role;
import com.gestmed.scheduling.entity.User;
import com.gestmed.scheduling.repository.RoleRepository;
import com.gestmed.scheduling.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(
        name = "gestmed.seed-demo-users",
        havingValue = "true"
)
public class DemoUserInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role doctorRole = findOrCreateRole("ROLE_DOCTOR");
        Role nurseRole = findOrCreateRole("ROLE_NURSE");
        Role patientRole = findOrCreateRole("ROLE_PATIENT");

        createUserIfMissing(
                "doctor",
                "doctor123",
                doctorRole
        );

        createUserIfMissing(
                "nurse",
                "nurse123",
                nurseRole
        );

        createUserIfMissing(
                "patient",
                "patient123",
                patientRole
        );
    }

    private Role findOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() ->
                        roleRepository.save(new Role(name))
                );
    }

    private void createUserIfMissing(
            String username,
            String rawPassword,
            Role role) {

        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User(
                username,
                passwordEncoder.encode(rawPassword),
                true
        );

        user.addRole(role);
        userRepository.save(user);
    }
}