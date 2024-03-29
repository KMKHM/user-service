package com.lucid.userservice.config.security;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;


import com.lucid.userservice.config.jwt.JwtFilter;
import com.lucid.userservice.config.jwt.TokenProvider;
import com.lucid.userservice.config.oauth.CustomOAuth2MemberService;
import com.lucid.userservice.config.oauth.OAuth2LoginFailureHandler;
import com.lucid.userservice.config.oauth.OAuth2LoginSuccessHandler;
import com.lucid.userservice.config.redis.RedisService;
import com.lucid.userservice.service.MemberService;
import com.lucid.userservice.service.MemberServiceImpl;
import com.lucid.userservice.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailService userDetailService;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final CustomDeniedHandler customDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2MemberService oAuth2MemberService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf((csrf) -> csrf.disable())
//                .authorizeHttpRequests((requests) -> requests.requestMatchers(antMatcher("/sign-up")).permitAll())
//                .authorizeHttpRequests((requests) -> requests.requestMatchers(antMatcher("/home")).permitAll())
//                .authorizeHttpRequests((requests) -> requests.requestMatchers(antMatcher("/**")).permitAll())
//                .authorizeHttpRequests((request) -> {
//                    request.requestMatchers(antMatcher("/auth")).authenticated();
//                })
                .authorizeHttpRequests((request) -> request.anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(customAuthenticationEntryPoint))
                .exceptionHandling(e -> e.accessDeniedHandler(customDeniedHandler))
                .csrf((csrf) -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .headers((e) -> e.frameOptions((a) -> a.sameOrigin()))
                .oauth2Login((oauth2) -> oauth2.userInfoEndpoint(
                        userInfoEndpointConfig -> userInfoEndpointConfig.userService(oAuth2MemberService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler))
                .with(new CustomFilterConfigurer(), Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {

        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            CustomAuthenticationFilter customAuthenticationFilter =
                    new CustomAuthenticationFilter(authenticationManager, userDetailService,
                            tokenProvider, redisService);
            customAuthenticationFilter.setFilterProcessesUrl("/login");
            customAuthenticationFilter.setAuthenticationSuccessHandler(new LoginSuccessHandler());
            customAuthenticationFilter.setAuthenticationFailureHandler(new LoginFailureHandler());

            JwtFilter jwtFilter = new JwtFilter(tokenProvider, redisService);


            builder.addFilter(customAuthenticationFilter)
                    .addFilterAfter(jwtFilter, CustomAuthenticationFilter.class);
        }
    }

}
