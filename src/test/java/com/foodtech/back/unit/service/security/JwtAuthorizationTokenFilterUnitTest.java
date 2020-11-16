package com.foodtech.back.unit.service.security;

import com.foodtech.back.entity.auth.Role;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.security.JwtAuthorizationTokenFilter;
import com.foodtech.back.security.JwtTokenService;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.security.JwtUserDetailsService;
import com.foodtech.back.util.ResponseCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultHeader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.servlet.FilterChain;
import java.time.LocalDateTime;
import java.util.Set;

import static com.foodtech.back.security.JwtAuthorizationTokenFilter.*;
import static com.foodtech.back.util.StringUtil.formFullMobileNumberStr;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("unit")
@TestPropertySource(value = {
        "classpath:sms.properties",
        "classpath:crypt.properties",
        "classpath:iiko.properties",
        "classpath:delivery.properties",
        "classpath:push.properties",
        "classpath:payment.properties",
        "classpath:client_messages.properties",
        "classpath:rabbitmq.properties",

        "classpath:payment-dev.properties"
})
class JwtAuthorizationTokenFilterUnitTest {

    @MockBean
    JwtUserDetailsService userDetailsService;

    @MockBean
    JwtTokenService jwtTokenService;

    private static final String COUNTRY_CODE = "7";
    private static final String MOBILE_NUMBER = "9999999999";
    private static final String AUTH_TOKEN = "AUTH_TOKEN";
    private static final String VALID_TOKEN_HEADER_VALUE = TOKEN_STARTS_WITH + AUTH_TOKEN;

    private static JwtUser jwtUser() {
        return new JwtUser(1L, MOBILE_NUMBER, COUNTRY_CODE, formFullMobileNumberStr(COUNTRY_CODE, MOBILE_NUMBER),
                Set.of(Role.ROLE_USER), new BonusAccount(), LocalDateTime.of(2019, 1, 1, 0, 0), true);
    }

    @Test
    void doFilter() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        JwtUser jwtUser = jwtUser();
        when(jwtTokenService.getFullMobileFromToken(AUTH_TOKEN)).thenReturn(jwtUser.getUsername());
        when(userDetailsService.loadUserByUsername(jwtUser.getUsername())).thenReturn(jwtUser);
        when(jwtTokenService.tokenIsValidForUser(AUTH_TOKEN, jwtUser)).thenReturn(true);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, VALID_TOKEN_HEADER_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNotNull(authentication);
        assertEquals(jwtUser, authentication.getPrincipal());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInvalidTokenHeader() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, "BearerRRRR AUTH_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterNoTokeHeader() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterMalformedToken() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        when(jwtTokenService.getFullMobileFromToken("INVALID_AUTH_TOKEN")).thenThrow(MalformedJwtException.class);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, TOKEN_STARTS_WITH + "INVALID_AUTH_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);

        assertEquals(ResponseCode.TOKEN_INVALID.toString(), request.getAttribute(TOKEN_ERROR));

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterExpiredToken() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);
        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(COUNTRY_CODE + "-" + MOBILE_NUMBER);
        ExpiredJwtException expiredJwtException = new ExpiredJwtException(new DefaultHeader(), claims, "Token expired");
        when(jwtTokenService.getFullMobileFromToken("EXPIRED_AUTH_TOKEN")).thenThrow(expiredJwtException);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, TOKEN_STARTS_WITH + "EXPIRED_AUTH_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);

        assertEquals(ResponseCode.TOKEN_EXPIRED.toString(), request.getAttribute(TOKEN_ERROR));

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterMobileNumberFromTokenIsNull() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        when(jwtTokenService.getFullMobileFromToken(AUTH_TOKEN)).thenReturn(null);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, VALID_TOKEN_HEADER_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterSecurityContextNotNull() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        JwtUser jwtUser = jwtUser();
        when(jwtTokenService.getFullMobileFromToken(AUTH_TOKEN)).thenReturn(jwtUser.getUsername());
        when(userDetailsService.loadUserByUsername(jwtUser.getUsername())).thenReturn(jwtUser);
        when(jwtTokenService.tokenIsValidForUser(AUTH_TOKEN, jwtUser)).thenReturn(true);


        SecurityContextImpl securityContext = new SecurityContextImpl();
        JwtUser anotherJwtUser = new JwtUser(2L, "4444444444", "7", "Another User",
                Set.of(Role.ROLE_ADMIN), new BonusAccount(), LocalDateTime.now(), true);
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(anotherJwtUser, "someCredentials"));
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, VALID_TOKEN_HEADER_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertEquals(anotherJwtUser, authentication.getPrincipal());

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterUserNotFound() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        JwtUser jwtUser = jwtUser();
        when(jwtTokenService.getFullMobileFromToken(AUTH_TOKEN)).thenReturn(jwtUser.getUsername());
        when(userDetailsService.loadUserByUsername(jwtUser.getUsername())).thenThrow(UsernameNotFoundException.class);
        when(jwtTokenService.tokenIsValidForUser(AUTH_TOKEN, jwtUser)).thenReturn(true);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, VALID_TOKEN_HEADER_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);

        assertEquals(ResponseCode.USER_NOT_FOUND.toString(), request.getAttribute(TOKEN_ERROR));

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterTokenInvalid() throws Exception {
        //when
        JwtAuthorizationTokenFilter tokenFilter = new JwtAuthorizationTokenFilter(userDetailsService, jwtTokenService);

        JwtUser jwtUser = jwtUser();
        when(jwtTokenService.getFullMobileFromToken(AUTH_TOKEN)).thenReturn(jwtUser.getUsername());
        when(userDetailsService.loadUserByUsername(jwtUser.getUsername())).thenReturn(jwtUser);
        when(jwtTokenService.tokenIsValidForUser(AUTH_TOKEN, jwtUser)).thenReturn(false);

        SecurityContextImpl securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        //do
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TOKEN_HEADER_KEY, VALID_TOKEN_HEADER_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        tokenFilter.doFilterInternal(request, response, chain);

        //then
        Authentication authentication = securityContext.getAuthentication();
        assertNull(authentication);

        verify(chain, times(1)).doFilter(request, response);
    }
}