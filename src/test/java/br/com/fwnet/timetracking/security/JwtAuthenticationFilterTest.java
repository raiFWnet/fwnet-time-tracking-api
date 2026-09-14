package br.com.fwnet.timetracking.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String EMAIL = "analyst@fwnet.com.br";
    private static final String TOKEN = "valid-token";

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtService,
                customUserDetailsService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectDisabledUserEvenWhenTokenIsValid() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL)
                .password("hashed-password")
                .roles("ANALYST")
                .disabled(true)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(TOKEN))
                .thenReturn(EMAIL);

        when(customUserDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateEnabledUserWhenTokenIsValid() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL)
                .password("hashed-password")
                .roles("ANALYST")
                .disabled(false)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(TOKEN))
                .thenReturn(EMAIL);

        when(customUserDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(TOKEN, userDetails))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(
                EMAIL,
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        verify(filterChain).doFilter(request, response);
    }
}