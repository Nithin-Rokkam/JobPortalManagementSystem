package com.capg.jobportal.dto;

<<<<<<< HEAD
/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: UserInfoResponse
 * DESCRIPTION:
 * This Data Transfer Object (DTO) is used to encapsulate
 * user-related information received from the Auth Service.
 *
 * It contains basic user details such as:
 * 1. User ID
 * 2. Name
 * 3. Email
 *
 * NOTE:
 * This DTO is primarily used for internal communication between
 * microservices.
 * ================================================================
 */
=======
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
public class UserInfoResponse {
    private Long id;
    private String name;
    private String email;
<<<<<<< HEAD

    public UserInfoResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
=======
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
