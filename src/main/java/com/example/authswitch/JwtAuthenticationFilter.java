package com.example.authswitch;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.authswitch.dto.TokenResponse;
import com.example.authswitch.token.JwtTokenService;
import com.example.authswitch.token.RefreshTokenStore;

import java.io.IOException;
import java.util.Collections;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	 @Autowired
	    private RefreshTokenStore refreshTokenStore;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String clientId=null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
            	Claims claims = jwtTokenService.parseToken(token);
                if (claims != null) {
                    clientId = claims.getSubject();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            clientId, null, null);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (ExpiredJwtException e) {
                
            	clientId = e.getClaims().getSubject();

                // Try to refresh the token
                String refreshToken = request.getHeader("X-Refresh-Token");
                String storedRefreshToken = refreshTokenStore.getRefreshToken(clientId);

                if (refreshToken != null && refreshToken.equals(storedRefreshToken)) {
                    TokenResponse newTokens = jwtTokenService.generateToken(clientId);
                    refreshTokenStore.storeRefreshToken(clientId, newTokens.getRefreshToken());

                    // Add new token to response header
                    response.setHeader("X-New-Access-Token", newTokens.getAccessToken());
                    response.setHeader("X-New-Refresh-Token", newTokens.getRefreshToken());

                    // Set new token context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(clientId, null, null);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

            	
            	
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header missing or malformed");
            return;
        }

        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/oauth/") || path.equals("/api/v1/register");
    }
    
}