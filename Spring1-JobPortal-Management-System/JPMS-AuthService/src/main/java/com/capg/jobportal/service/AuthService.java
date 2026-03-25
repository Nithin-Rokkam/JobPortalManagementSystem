package com.capg.jobportal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.dao.UserRepository;
import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.LoginRequest;
import com.capg.jobportal.dto.RegisterRequest;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.entity.User;
import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;
import com.capg.jobportal.exception.ResourceNotFoundException;
import com.capg.jobportal.exception.UserAlreadyExistsException;
import com.capg.jobportal.security.JwtUtil;
import com.capg.jobportal.util.CloudinaryUtil;

@Service
public class AuthService {
	
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CloudinaryUtil cloudinaryUtil;

    
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       CloudinaryUtil cloudinaryUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.cloudinaryUtil = cloudinaryUtil;
    }

	
	
	public AuthResponse register(RegisterRequest request) {
		
		if(request.getRole() == Role.ADMIN) {
			throw new IllegalArgumentException("Admin registration is not allowed");
		}
		
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
        }
 
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());

        userRepository.save(user);

//        rabbitTemplate.convertAndSend(
//    	    RabbitMQConfig.EXCHANGE,
//    	    RabbitMQConfig.USER_REGISTERED_KEY,
//    	    new UserRegisteredEvent(user.getId(), user.getName(), user.getEmail(), user.getRole().name())
//    	);
        
        return new AuthResponse("Registration successful. Please login.");
    }	
	
	
	public AuthResponse login(LoginRequest request) {
	    User user = userRepository.findByEmail(request.getEmail()).orElse(null);

	    if (user == null) {
	        throw new IllegalArgumentException("Invalid credentials");
	    }

	    if (user.getStatus() == UserStatus.BANNED) {
	        throw new IllegalArgumentException("Account suspended");
	    }

	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new IllegalArgumentException("Invalid credentials");
	    }

	    String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
	    String refreshToken = jwtUtil.generateRefreshToken();

	    user.setRefreshToken(refreshToken);
	    userRepository.save(user);

	    return new AuthResponse(accessToken, refreshToken, user.getRole().name(), user.getId(), user.getName(), user.getEmail());
	}
	

	public AuthResponse refresh(String refreshToken) {
	    User user = userRepository.findByRefreshToken(refreshToken).orElse(null);

	    if (user == null) {
	        throw new ResourceNotFoundException("Invalid or expired refresh token");
	    }

	    if (user.getStatus() == UserStatus.BANNED) {
	        throw new IllegalArgumentException("Account suspended");
	    }

	    String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
	    String newRefreshToken = jwtUtil.generateRefreshToken();

	    user.setRefreshToken(newRefreshToken);
	    userRepository.save(user);

	    return new AuthResponse(newAccessToken, newRefreshToken, user.getRole().name(), user.getId(), user.getName(), user.getEmail());
	}
	

	public void logout(String refreshToken) {
	    User user = userRepository.findByRefreshToken(refreshToken).orElse(null);

	    if (user == null) {
	        throw new ResourceNotFoundException("Invalid refresh token");
	    }

	    user.setRefreshToken(null);
	    userRepository.save(user);
	}
	
	
	public String updateProfilePicture(Long userId, MultipartFile picture) throws IOException {
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        throw new ResourceNotFoundException("User not found");
	    }
	    String url = cloudinaryUtil.uploadProfilePicture(picture);
	    user.setProfilePictureUrl(url);
	    userRepository.save(user);
	    return url;
	}

	
	public String updateProfileResume(Long userId, MultipartFile resume) throws IOException {
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        throw new ResourceNotFoundException("User not found");
	    }
	    String url = cloudinaryUtil.uploadResume(resume);
	    user.setResumeUrl(url);
	    userRepository.save(user);
	    return url;
	}

	
	public UserProfileResponse getProfile(Long userId) {
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        throw new ResourceNotFoundException("User not found");
	    }
	    return UserProfileResponse.fromEntity(user);
	}
	
	
	public List<UserProfileResponse> getAllUsers() {
	    List<User> users = userRepository.findAll();
	    List<UserProfileResponse> result = new ArrayList<>();
	    for (User user : users) {
	        result.add(UserProfileResponse.fromEntity(user));
	    }
	    return result;
	}

	public void deleteUser(Long userId) {
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        throw new ResourceNotFoundException("User not found with id: " + userId);
	    }
	    userRepository.delete(user);
	}

	public void updateUserStatus(Long userId, String status) {
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        throw new ResourceNotFoundException("User not found with id: " + userId);
	    }
	    user.setStatus(UserStatus.valueOf(status));
	    user.setRefreshToken(null);
	    userRepository.save(user);
	}

	public void invalidateTokenByUserId(Long userId) {
	    User user = userRepository.findById(userId).orElse(null);
	    if (user != null) {
	        user.setRefreshToken(null);
	        userRepository.save(user);
	    }
	}
	
}
