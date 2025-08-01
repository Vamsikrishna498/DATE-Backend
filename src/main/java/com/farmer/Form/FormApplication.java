package com.farmer.Form;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.farmer.Form.Entity.User;
import com.farmer.Form.Entity.Role;
import com.farmer.Form.Entity.UserStatus;
import com.farmer.Form.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@SpringBootApplication
public class FormApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormApplication.class, args);
	}

	@Bean
	public CommandLineRunner createSuperAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String superAdminEmail = "projecthinfintiy@12.in";
			if (userRepository.findByEmail(superAdminEmail).isEmpty()) {
				User superAdmin = new User();
				superAdmin.setName("Super");
				superAdmin.setEmail(superAdminEmail);
				superAdmin.setPhoneNumber("9999999999");
				superAdmin.setPassword(passwordEncoder.encode("Password123@"));
				superAdmin.setDateOfBirth(LocalDate.of(1990, 1, 1));
				superAdmin.setGender("Other");
				superAdmin.setRole(Role.SUPER_ADMIN);
				superAdmin.setStatus(UserStatus.APPROVED);
				superAdmin.setForcePasswordChange(false);
				userRepository.save(superAdmin);
				System.out.println("Super admin created with email: " + superAdminEmail);
			}
		};
	}
}
