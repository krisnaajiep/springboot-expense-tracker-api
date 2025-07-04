package com.krisnaajiep.expensetrackerapi.service;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 02.53
@Last Modified 27/06/25 02.53
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.ConflictException;
import com.krisnaajiep.expensetrackerapi.security.CustomUserDetails;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service class responsible for handling authentication and user management processes.
 * AuthService provides methods for registering new users and authenticating
 * existing users. It integrates with UserRepository for database interactions,
 * PasswordEncoder for secure password handling, JwtUtility for token generation,
 * and AuthenticationManager for credential validation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    /**
     * A repository interface for performing CRUD operations and custom queries on the User entity.
     * This instance is used in the AuthService class to interact with the underlying database to
     * verify the existence of users, retrieve user information, and save new users.
     */
    private final UserRepository userRepository;

    /**
     * An instance of the PasswordEncoder interface used for encoding and decoding
     * user passwords securely. This is used during user registration to encode
     * passwords before saving them to the database and for password authentication purposes.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * An instance of the JwtUtility class used for handling operations related to
     * JSON Web Tokens (JWT). This includes generating tokens with specific claims,
     * parsing and validating tokens, and extracting information such as email from
     * a given token. It is used in the AuthService class for managing secure
     * authentication and authorization workflows.
     */
    private final JwtUtility jwtUtility;

    /**
     * An instance of the AuthenticationManager interface responsible for managing
     * the authentication process of users during login. It is used to verify the
     * provided credentials (email and password) and authenticate the user accordingly.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user into the system. If a user with the same email already exists,
     * a ConflictException is thrown. The user's password is securely encoded before being
     * saved. Upon successful registration, an access token is generated and returned.
     *
     * @param user the User object containing the user details to be registered
     * @return a TokenResponseDto object containing the generated access token for the registered user
     * @throws ConflictException if a user with the same email already exists
     */
    public TokenResponseDto register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with this email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String accessToken = jwtUtility.generateToken(savedUser.getId().toString(), savedUser.getEmail());

        return new TokenResponseDto(accessToken);
    }

    /**
     * Authenticates a user with the provided email and password credentials. If authentication is
     * successful, a JWT access token is generated and returned.
     *
     * @param email the email of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return a TokenResponseDto object containing the generated access token for the authenticated user
     */
    public TokenResponseDto login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtility.generateToken(userDetails.getId().toString(), userDetails.getUsername());

        return new TokenResponseDto(accessToken);
    }
}
