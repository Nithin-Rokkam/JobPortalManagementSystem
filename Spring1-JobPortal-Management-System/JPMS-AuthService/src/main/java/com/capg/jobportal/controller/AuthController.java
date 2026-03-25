	package com.capg.jobportal.controller;
	
	import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.LoginRequest;
import com.capg.jobportal.dto.RegisterRequest;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.service.AuthService;

import jakarta.validation.Valid;
	
	@RestController
	@RequestMapping("/api/auth")
	public class AuthController {
	
	    private final AuthService authService;
	
	    public AuthController(AuthService authService) {
	        this.authService = authService;
	    }
	
	    
	    @PostMapping("/register")
	    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
	        AuthResponse response = authService.register(request);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	
	    
	    @PostMapping("/login")
	    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
	        AuthResponse response = authService.login(request);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	
	    
	    @PostMapping("/refresh")
	    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
	        String refreshToken = body.get("refreshToken");
	        AuthResponse response = authService.refresh(refreshToken);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	
	    
	    @PostMapping("/logout")
	    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body) {
	        String refreshToken = body.get("refreshToken");
	        authService.logout(refreshToken);
	        return new ResponseEntity<>(Map.of("message", "Logged out successfully"), HttpStatus.OK);
	    }
	
	    
	    @PutMapping("/profile/picture")
	    public ResponseEntity<Map<String, String>> uploadProfilePicture(
	            @RequestPart("picture") MultipartFile picture,
	            @RequestHeader("X-User-Id") Long userId) throws IOException {
	    	
	        String url = authService.updateProfilePicture(userId, picture);
	        Map<String, String> response = Map.of("profilePictureUrl", url);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	
	    
	    @PutMapping("/profile/resume")
	    public ResponseEntity<Map<String, String>> uploadResume(
	            @RequestPart("resume") MultipartFile resume,
	            @RequestHeader("X-User-Id") Long userId) throws IOException {
	        String url = authService.updateProfileResume(userId, resume);
	        Map<String, String> response = Map.of("resumeUrl", url);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	
	    
	    @GetMapping("/profile")
	    public ResponseEntity<UserProfileResponse> getProfile(
	            @RequestHeader("X-User-Id") Long userId) {
	        UserProfileResponse response = authService.getProfile(userId);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	    
	}