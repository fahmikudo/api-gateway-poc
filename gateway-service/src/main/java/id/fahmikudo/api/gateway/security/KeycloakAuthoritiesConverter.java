package id.fahmikudo.api.gateway.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class KeycloakAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Set<GrantedAuthority> authorities = new HashSet<>(defaultConverter.convert(source));
        authorities.addAll(extractScopeAuthorities(source));
        authorities.addAll(extractRealmRoleAuthorities(source));
        return authorities;
    }

    private Collection<GrantedAuthority> extractScopeAuthorities(Jwt jwt) {
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim instanceof String scopeString) {
            return Stream.of(scopeString.split(" "))
                    .filter(s -> !s.isBlank())
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toSet());
        }
        return List.of();
    }

    private Collection<GrantedAuthority> extractRealmRoleAuthorities(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmMap) {
            Object roles = realmMap.get("roles");
            if (roles instanceof Collection<?> roleCollection) {
                return roleCollection.stream()
                        .filter(role -> role instanceof String)
                        .map(role -> new SimpleGrantedAuthority("SCOPE_" + role))
                        .collect(Collectors.toSet());
            }
        }
        return List.of();
    }
}

