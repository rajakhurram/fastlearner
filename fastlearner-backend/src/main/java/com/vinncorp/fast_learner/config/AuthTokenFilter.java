package com.vinncorp.fast_learner.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.models.permission.SubscriptionUrlPermission;
import com.vinncorp.fast_learner.models.permission.UrlPermission;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.services.permission.subcription_url_permission.ISubscriptionUrlPermissionService;
import com.vinncorp.fast_learner.services.permission.url_permission.IUrlPermissionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.token.ITokenService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Constants.Text;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private ITokenService tokenService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ISubscribedUserService subscribedUserService;
    @Autowired
    private IUrlPermissionService urlPermissionService;
    @Autowired
    private ISubscriptionUrlPermissionService subscriptionUrlPermissionService;
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (isPublicRequest(request) && JwtUtils.parseJwt(request) == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = null;
        if (request.getRequestURI().startsWith(APIUrls.NOTIFICATION + "/register") ||
                request.getRequestURI().startsWith(APIUrls.CERTIFICATE + APIUrls.DOWNLOAD_CERTIFICATE)) {
            jwt = Objects.isNull(request.getParameter("token")) || request.getParameter("token").isEmpty() ? null : request.getParameter("token");
        }else{
            jwt = JwtUtils.parseJwt(request);
        }
        try {
            boolean isTokenExists = false;
            UrlPermission urlPermission = null;
            String userName = "";
            boolean tokenValid = false;
            boolean isTokenValid = false;
            HttpStatus status = null;
            if(Objects.nonNull(JwtUtils.parseJwt(request)) || Objects.nonNull(jwt)){
                String urlBasePath = this.getUrlBasePath(request);
                isTokenExists = tokenService.existsByToke(JwtUtils.parseJwt(request));
                urlPermission = this.urlPermissionService.findByMethodAndStatusAndStartsWithUrl(request.getMethod(), GenericStatus.ACTIVE, urlBasePath.substring(APIUrls.API_MAIN.length()));
                userName = jwtUtils.getUserNameFromJwtToken(jwt);
                tokenValid = jwtUtils.validateToken(jwt);
            }

            if((Objects.isNull(JwtUtils.parseJwt(request)) && Objects.isNull(jwt)) || isTokenExists) {
                status = HttpStatus.FORBIDDEN;
            } else if (jwt != null && tokenValid && Objects.nonNull(urlPermission)) {
                SubscribedUser subscribedUser = this.subscribedUserService.findByUser(userName);
                SubscriptionUrlPermission subscriptionUrlPermission = this.subscriptionUrlPermissionService.findBySubscriptionAndUrlPermissionAndStatus(subscribedUser.getSubscription().getId(), urlPermission.getId(), GenericStatus.ACTIVE);
                if (Objects.isNull(subscriptionUrlPermission)) {
                    status = HttpStatus.FORBIDDEN;
                } else {
                    isTokenValid = true;
                    status = HttpStatus.OK;
                }
            } else if (jwt != null && tokenValid) {
                isTokenValid = true;
                status = HttpStatus.OK;

            } else {
                status = HttpStatus.FORBIDDEN;
            }

            if(isTokenValid){
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }else{
                Message<String> errorResponse = new Message<String>()
                        .setStatus(status.value())
                        .setCode(status.toString())
                        .setMessage(Text.JWT_TOKEN_IS_INVALID);
                response.setStatus(status.value());
                response.getWriter().write(convertObjectToJson(errorResponse));
            }

        } catch (ExpiredJwtException | BadCredentialsException ex) {
            logger.error(Text.JWT_TOKEN_IS_EXPIRED);
            request.setAttribute(Text.EXCEPTION, ex);
            Message<String> errorResponse = new Message<String>()
                    .setStatus(HttpStatus.UNAUTHORIZED.value())
                    .setCode(HttpStatus.UNAUTHORIZED.toString())
                    .setMessage(Text.JWT_TOKEN_IS_EXPIRED);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error(Text.AUTHENTICATION, e);
            log.error(e.getMessage());
            request.setAttribute(Text.EXCEPTION, e);
            Message<String> errorResponse = new Message<String>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                    .setMessage(e.getLocalizedMessage());

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // List of public API paths
        List<String> publicPaths = Arrays.asList(
                APIUrls.AUTHENTICATION_MAIN,
                APIUrls.AUTHENTICATION_MAIN + APIUrls.DO_LOGOUT,
                APIUrls.COURSE_CATEGORY_API,
                APIUrls.COURSE_API + APIUrls.GET_ALL_COURSES_BY_CATEGORY,
                APIUrls.COURSE_API + "/get/",
                APIUrls.COURSE_API + APIUrls.GET_RELATED_COURSES,
                APIUrls.COURSE_API + APIUrls.SEARCH_BY_FILTER,
                APIUrls.COURSE_API + APIUrls.SEARCH_AUTOCOMPLETE,
                APIUrls.COURSE_API + APIUrls.UNIQUE_COURSE_TITLE,
                APIUrls.COURSE_API + APIUrls.COURSE_URL,
                APIUrls.CERTIFICATE + "/verify",
                APIUrls.PAYMENT_GATEWAY_WEBHOOK,
                APIUrls.DOWNLOADER + APIUrls.DOWNLOAD,
                APIUrls.NEWSLETTER_SUBSCRIPTION + APIUrls.SUBSCRIBE_NEWSLETTER,
                APIUrls.USER_PROFILE_API + APIUrls.GET_USER_PROFILE_API,
                APIUrls.COURSE_API + APIUrls.ES_COURSE_SEARCH,
                APIUrls.COURSE_API + APIUrls.GET_ALL_COURSES_BY_INSTRUCTOR_FOR_PROFILE,
                APIUrls.CONTACT_US + APIUrls.SUBMIT,
                "/his",
                APIUrls.HOME_PAGE,
                APIUrls.AFFILIATE +APIUrls.STRIPE_REDIRECT_URL,
                APIUrls.USER_SESSION + APIUrls.USER_SESSION_TOKEN,
                APIUrls.STATIC_PAGE + "/"
        );

        // Check if the URI matches any public path
        for (String publicPath : publicPaths) {
            if (uri.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private String getUrlBasePath(HttpServletRequest request) {
        HandlerMethod handlerMethod = null;
        try {
            handlerMethod = (HandlerMethod) handlerMapping.getHandler(request).getHandler();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RequestMapping classRequestMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
        String classLevelPath = (classRequestMapping != null && classRequestMapping.value().length > 0)
                ? classRequestMapping.value()[0]
                : "";
        RequestMapping methodRequestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
        String methodLevelPath = (methodRequestMapping != null && methodRequestMapping.value().length > 0)
                ? methodRequestMapping.value()[0]
                : "";
        return (classLevelPath + methodLevelPath).replaceAll("\\{[^/]+}", "");
    }

}
