package com.sp.fc.web.config;

import com.sp.fc.user.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@RequiredArgsConstructor
public class OnlinePaperSecurityConfigure extends WebSecurityConfigurerAdapter {

    private final UserSecurityService userSecurityService;
    private final DataSource dataSource;


    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        repository.setCreateTableOnStartup(true);
        return repository;
    }

    @Bean
    PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices(){
        PersistentTokenBasedRememberMeServices services =
                new PersistentTokenBasedRememberMeServices("paper-site-remember-me",
                        userSecurityService,tokenRepository());
        return services;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userSecurityService)
                .passwordEncoder(passwordEncoder());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final SpLoginFilter filter = new SpLoginFilter(
            authenticationManagerBean(),
                persistentTokenBasedRememberMeServices()
        );
        http
                .csrf().disable()
                .formLogin(login ->{
                    login.loginPage("/login");
                })
                .logout(logout ->{
                    logout.logoutSuccessUrl("/");
                    logout.logoutUrl("/logout");
                })
                .rememberMe(r -> r.rememberMeServices(persistentTokenBasedRememberMeServices()))
                .addFilterAt(filter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception ->{
                    exception.accessDeniedPage("/access-denied");
                })
                .authorizeRequests(config->{
                    config
                            .antMatchers("/").permitAll()
                            .antMatchers("/login").permitAll()
                            .antMatchers("/signup/*").permitAll()
                            .antMatchers("/error").permitAll()
                            .antMatchers("/study/**").hasAnyAuthority("ROLE_ADMIN","ROLE_STUDENT")
                            .antMatchers("/teacher/**").hasAnyAuthority("ROLE_ADMIN","ROLE_TEACHER")
                            .antMatchers("/manager/**").hasAnyAuthority("ROLE_ADMIN")
                    ;
                })
        ;
    }


}
