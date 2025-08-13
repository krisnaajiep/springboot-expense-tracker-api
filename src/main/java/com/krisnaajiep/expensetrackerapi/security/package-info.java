/**
 * This package contains security-related components that handle authentication 
 * and authorization for the application. It includes:
 * <ul>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.CustomUserDetails} to represents the details of a user account</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.JwtUtility} for JWT token generation and validation</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.JwtAuthenticationToken} to create authentication based on JWT</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.JwtAuthenticationProvider} for authenticating user credential based on JWT</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.JwtAuthenticationFilter} to handle JWT authentication</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.CustomAuthenticationEntryPoint} to handle unauthenticated requests</li>
 *     <li>{@link com.krisnaajiep.expensetrackerapi.security.AuthenticationFailureListener} for authentication failure event</li>
 * </ul>
 *
 * @since 0.0.1
 * @see com.krisnaajiep.expensetrackerapi.security.service
 * @see com.krisnaajiep.expensetrackerapi.security.config
 */
package com.krisnaajiep.expensetrackerapi.security;