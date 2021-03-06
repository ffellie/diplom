package app.expert.configs;

import app.base.services.GContextService;
import app.expert.db.manager.ManagerCache;
import app.expert.db.statics.route.Route;
import app.expert.db.statics.route.RouteRepository;
import app.expert.filters.UserAuthorizationFilter;
import app.expert.services.CookieService;
import app.expert.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.List;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{

    public final static int SESSION_LIFETIME_TIME_SECONDS = 60 * 60;

    private final GContextService context;
    private final CookieService cookieService;
    private final SessionService sessionService;
    private final ManagerCache managerCache;
    private final RouteRepository routeRepository;


    @Value("${spring.boot.admin.client.metadata.user.name:admin}")
    private String adminUser;

    @Value("${spring.boot.admin.client.metadata.user.password:th1s1s4dm1n}")
    private String adminPassword;

    @Override
    public void configure(WebSecurity web) {
        List<String> ignored = routeRepository.findAllByOpen(true).stream().map(Route::getPath).collect(Collectors.toList());
        String[] arr = new String[ignored.size()];
        arr = ignored.toArray(arr);
        web.ignoring()
                .antMatchers(arr);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        UserAuthorizationFilter clientAuthorizationFilter =
                new UserAuthorizationFilter(authenticationManager(),
                        context, cookieService, sessionService, managerCache);

        http.cors().and().csrf().ignoringAntMatchers().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .addFilter(clientAuthorizationFilter)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}

