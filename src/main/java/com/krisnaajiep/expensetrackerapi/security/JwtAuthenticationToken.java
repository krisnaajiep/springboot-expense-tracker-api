package com.krisnaajiep.expensetrackerapi.security;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 08.16
@Last Modified 30/06/25 08.16
Version 1.0
*/

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final Object principal;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     * @param token the JWT token
     * @param principal the principal (user details) associated with this token
     */
    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, String token, Object principal) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        super.setAuthenticated(true);
    }

    /**
     * Creates a token with no authorities.
     * This constructor is used when the token is being created without any authentication.
     *
     * @param token the JWT token
     */
    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.principal = null;
        super.setAuthenticated(false); // Not authenticated yet
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
