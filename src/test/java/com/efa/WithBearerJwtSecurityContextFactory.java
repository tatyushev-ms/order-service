package com.efa;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class WithBearerJwtSecurityContextFactory implements WithSecurityContextFactory<WithBearerJwt> {
    
    @Override
    public SecurityContext createSecurityContext(WithBearerJwt annotation) {
        final SecurityContext result = SecurityContextHolder.createEmptyContext();
        
        final Map<String, Object> attributes = processAttributes(annotation.attributes());
        final OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", null, null);
        final Collection<GrantedAuthority> authorities = Collections.emptyList();
        
        final OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(attributes, authorities);
        final BearerTokenAuthentication auth = new BearerTokenAuthentication(principal, accessToken, authorities);
        result.setAuthentication(auth);
        return result;
    }
    
    private Map<String, Object> processAttributes(WithBearerJwt.Attribute[] attributes) {
        if (attributes == null || attributes.length == 0) {
            return Collections.emptyMap();
        }
        final Map<String, Object> result = new TreeMap<>();
        for (final WithBearerJwt.Attribute attribute : attributes) {
            result.put(attribute.name(), attribute.value());
        }
        return result;
    }
    
}
