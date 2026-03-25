package com.capg.jobportal.dto;

public class AuthResponse {
	
	private String message;
	private String accessToken;
    private String refreshToken;
    private String role;
    private Long userId;
    private String name;
    private String email;
 
  
    public AuthResponse() {
    	
    }
    
    
    public AuthResponse(String message) {
        this.message = message;
    }
    
    
    public AuthResponse(String accessToken, String refreshToken, String role, Long userId, String name, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
    
    

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
}
