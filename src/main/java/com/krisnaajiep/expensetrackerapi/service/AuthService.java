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

import com.krisnaajiep.expensetrackerapi.config.AuthConfig;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.ConflictException;
import com.krisnaajiep.expensetrackerapi.handler.exception.UnauthorizedException;
import com.krisnaajiep.expensetrackerapi.model.RefreshToken;
import com.krisnaajiep.expensetrackerapi.repository.RefreshTokenRepository;
import com.krisnaajiep.expensetrackerapi.security.CustomUserDetails;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service class responsible for handling authentication and user management processes.
 * AuthService provides methods for registering new users, authenticating existing users,
 * and refresh tokens. It integrates with UserRepository and RefreshTokenRepository for
 * database interactions, PasswordEncoder for secure password handling, JwtUtility for
 * token generation, and AuthenticationManager for credential validation.
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

    private final RefreshTokenRepository refreshTokenRepository;

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

    private final AuthConfig authConfig;

    /**
     * Registers a new user into the system. If a user with the same email already exists,
     * a ConflictException is thrown. The user's password is securely encoded before being
     * saved. Upon successful registration, an access token is generated and returned.
     *
     * @param user the User object containing the user details to be registered
     * @return a TokenResponseDto object containing the generated access token and refresh token for the registered user
     * @throws ConflictException if a user with the same email already exists
     */
    public TokenResponseDto register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with this email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String accessToken = jwtUtility.generateToken(savedUser.getId().toString(), savedUser.getEmail());

        String rawRefreshToken = SecureRandomUtility.generateRandomString(32);
        RefreshToken refreshToken = createRefreshToken(savedUser, rawRefreshToken);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponseDto(accessToken, rawRefreshToken);
    }

    /**
     * Authenticates a user with the provided email and password credentials. If authentication is
     * successful, a JWT access token is generated and returned.
     *
     * @param email the email of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return a TokenResponseDto object containing the generated access token and refresh token for the authenticated user
     */
    public TokenResponseDto login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtility.generateToken(userDetails.getId().toString(), userDetails.getUsername());

        refreshTokenRepository.deleteAllByUserId(userDetails.getId());
        String rawRefreshToken = SecureRandomUtility.generateRandomString(32);
        RefreshToken refreshToken = createRefreshToken(userDetails.user(), rawRefreshToken);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponseDto(accessToken, rawRefreshToken);
    }

    /**
     * Refreshes the access token and generates a new refresh token using the provided refresh token.
     * Validates the provided token and checks its expiration status. If valid, it generates a new
     * access token linked to the associated user and replaces the expired refresh token with a new one.
     *
     * @param token the refresh token provided by the user
     * @return a TokenResponseDto object containing the newly generated access token and refresh token
     * @throws UnauthorizedException if the provided refresh token is invalid or expired
     */
    public TokenResponseDto refreshToken(String token) {
        String hashedToken = DigestUtils.sha256Hex(token);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtUtility.generateToken(user.getId().toString(), user.getEmail());

        String newRefreshToken = SecureRandomUtility.generateRandomString(32);
        refreshToken.setToken(DigestUtils.sha256Hex(newRefreshToken));
        refreshToken.setExpiryDate(Instant.now().plusMillis(authConfig.getRefreshTokenExpiration()));
        refreshToken.setRotatedAt(Instant.now());

        return new TokenResponseDto(accessToken, newRefreshToken);
    }

    /**
     * Creates a new refresh token for a given user and token string. The method
     * generates a hashed version of the provided token, sets an expiration date,
     * and associates the token with the specified user.
     *
     * @param user the User object with whom the refresh token will be associated
     * @param token the raw token string to be hashed and stored
     * @return a newly created RefreshToken object containing a hashed token and expiry date
     */
    private RefreshToken createRefreshToken(User user, String token) {
        return RefreshToken.builder()
                .user(user)
                .token(DigestUtils.sha256Hex(token))
                .expiryDate(Instant.now().plusMillis(authConfig.getRefreshTokenExpiration()))
                .build();
    }
}
