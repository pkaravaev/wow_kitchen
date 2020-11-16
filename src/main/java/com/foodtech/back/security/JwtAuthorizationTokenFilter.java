package com.foodtech.back.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.foodtech.back.util.ResponseCode.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;
    public static final String TOKEN_HEADER_KEY = "Authorization";
    public static final String TOKEN_STARTS_WITH = "Bearer ";
    public static final String TOKEN_ERROR = "tokenError";

    public JwtAuthorizationTokenFilter(@Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService,
                                       JwtTokenService jwtTokenService) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        log.debug("Processing authentication for request: '{}'", request.getRequestURL());
        final String requestHeader = request.getHeader(TOKEN_HEADER_KEY);
        if (nonNull(requestHeader) && requestHeader.startsWith(TOKEN_STARTS_WITH)) {
            String token = requestHeader.substring(TOKEN_STARTS_WITH.length());
            processTokenAuthentication(request, token);
        }
        chain.doFilter(request, response);
    }

    private void processTokenAuthentication(HttpServletRequest request, String token) {
        String fullMobileNumber = parseMobileFromToken(request, token);
        if (nonNull(fullMobileNumber) && isNull(SecurityContextHolder.getContext().getAuthentication())) {
            loadUserAndAuthenticate(request, fullMobileNumber, token);
        }
    }

    private String parseMobileFromToken(HttpServletRequest request, String token) {
        try {
            return jwtTokenService.getFullMobileFromToken(token);
        } catch (IllegalArgumentException | MalformedJwtException e) {
            // Ошибки в части авторизации через токен перехватываются в JwtAuthenticationEntryPoint,
            // для того, чтобы получить более подробное описание ошибки, передаем его в аттрибуте tokenError
            request.setAttribute(TOKEN_ERROR, TOKEN_INVALID.toString());
            log.error("An error occurred during getting mobile from token. Cause: '{}'", e.getMessage());
        } catch (ExpiredJwtException e) {
            request.setAttribute(TOKEN_ERROR, TOKEN_EXPIRED.toString());
            log.warn("Authentication failed. Token is expired. User: '{}'", e.getClaims().getSubject());
        }

        return null;
    }

    private void loadUserAndAuthenticate(HttpServletRequest request, String mobile, String token) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(mobile);
            checkTokenAndAuthenticateUser(request, token, userDetails);
        } catch (UsernameNotFoundException e) {
            request.setAttribute(TOKEN_ERROR, USER_NOT_FOUND.toString());
            log.error("Authentication failed. User '{}' not found", mobile);
        }
    }

    private void checkTokenAndAuthenticateUser(HttpServletRequest request, String token, UserDetails user) {
        if (jwtTokenService.tokenIsValidForUser(token, (JwtUser) user)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication success. User: '{}'", user.getUsername());
        } else {
            log.warn("Authentication failed. Invalid token. User: '{}'", user.getUsername());
        }
    }
}
