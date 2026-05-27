package com.capg.jobportal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capg.jobportal.entity.User;
import com.capg.jobportal.enums.Role;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: UserRepository
 * DESCRIPTION:
 * This repository interface provides database access operations
 * for the User entity using Spring Data JPA.
 *
 * It extends JpaRepository to inherit standard CRUD operations
 * and defines additional custom query methods for user-specific
 * operations such as authentication and validation.
 *
 * KEY FEATURES:
 * - User lookup by email
 * - Email existence validation
 * - Refresh token-based user retrieval
 *
 * PURPOSE:
 * Acts as the DAO layer for interacting with the "users" table,
 * supporting authentication and user management functionalities.
 * ================================================================
 */

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
	
	/*
     * ================================================================
     * METHOD: findByEmail
     * DESCRIPTION:
     * Retrieves a user based on their email address.
     *
     * Used primarily during login/authentication and profile lookup.
     *
     * PARAMETERS:
     * - email → user's email address
     *
     * RETURNS:
     * - Optional<User> (empty if user not found)
     * ================================================================
     */
	Optional<User> findByEmail(String email);
	 
	
	/*
     * ================================================================
     * METHOD: existsByEmail
     * DESCRIPTION:
     * Checks whether a user with the given email already exists.
     *
     * Used during registration to prevent duplicate accounts.
     *
     * PARAMETERS:
     * - email → user's email address
     *
     * RETURNS:
     * - boolean (true if exists, false otherwise)
     * ================================================================
     */
    boolean existsByEmail(String email);
 
    
    /*
     * ================================================================
     * METHOD: findByRefreshToken
     * DESCRIPTION:
     * Retrieves a user based on their refresh token.
     *
     * Used in authentication flow to validate and generate new
     * access tokens during token refresh operations.
     *
     * PARAMETERS:
     * - refreshToken → stored refresh token of user
     *
     * RETURNS:
     * - Optional<User> (empty if token is invalid or not found)
     * ================================================================
     */
    Optional<User> findByRefreshToken(String refreshToken);
    
    
    List<User> findByRole(Role role);
}
