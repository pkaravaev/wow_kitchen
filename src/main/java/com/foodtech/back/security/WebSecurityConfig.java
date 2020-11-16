package com.foodtech.back.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Configuration
    @Order(1)
    public class AppWebSecurityConfig extends WebSecurityConfigurerAdapter {

        private final JwtAuthenticationEntryPoint unauthorizedHandler;

        private final JwtUserDetailsService jwtUserDetailsService;

        private final JwtTokenService tokenUtil;

        public AppWebSecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                                    JwtUserDetailsService jwtUserDetailsService, JwtTokenService tokenUtil) {
            this.unauthorizedHandler = unauthorizedHandler;
            this.jwtUserDetailsService = jwtUserDetailsService;
            this.tokenUtil = tokenUtil;
        }

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    // we don't need CSRF because our token is invulnerable
                    .csrf().disable()
                    .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                    // don't create session
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .antMatcher("/app/**")
                    .authorizeRequests().anyRequest().authenticated().and()
                    .addFilterBefore(new JwtAuthorizationTokenFilter(jwtUserDetailsService, tokenUtil),
                            UsernamePasswordAuthenticationFilter.class);
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // AuthenticationTokenFilter will ignore the below paths
            web
                    .ignoring().antMatchers("/app/public/**").and()
                    .ignoring().antMatchers("/*.html", "/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js");
        }

    }


    // Basic auth конфигурация для
    @Configuration
    @Order(2)
    public class AdminWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Bean
        public PasswordEncoder passwordEncoderBean() {
            return new BCryptPasswordEncoder();
        }

        final AdminDetailsService adminDetailsService;

        public AdminWebSecurityConfig(AdminDetailsService adminDetailsService) {
            this.adminDetailsService = adminDetailsService;
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authenticationBuilder) throws Exception {
            authenticationBuilder.userDetailsService(adminDetailsService).passwordEncoder(passwordEncoderBean());
        }

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    // we don't need CSRF because our token is invulnerable
                    .csrf().disable()
                    // don't create session
                    .requestMatchers()
                    .antMatchers("/")
                    .antMatchers("/admin/**")
                    .antMatchers("/master/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/").hasAnyRole("ADMIN", "MASTER")
                    .antMatchers("/admin/**").hasAnyRole("ADMIN", "MASTER")
                    .antMatchers("/master/**").hasRole("MASTER")
                    .antMatchers("/public/**").permitAll()
                    .antMatchers("/resources/**").permitAll()
                    .and()
                    .formLogin()
                    .permitAll();
        }


    }
}
