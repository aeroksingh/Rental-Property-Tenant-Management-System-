package com.rentalmanagement.system.security;

import com.rentalmanagement.system.entity.Owner;
import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Unified Spring Security principal for both Owner and Tenant, since they are
 * separate JPA entities/tables but share the same authentication mechanism.
 */
@Getter
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Role role;

    public AppUserPrincipal(Long id, String email, String password, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static AppUserPrincipal fromOwner(Owner owner) {
        return new AppUserPrincipal(owner.getId(), owner.getEmail(), owner.getPassword(), owner.getRole());
    }

    public static AppUserPrincipal fromTenant(Tenant tenant) {
        return new AppUserPrincipal(tenant.getId(), tenant.getEmail(), tenant.getPassword(), tenant.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
