package com.rentalmanagement.system.security;

import com.rentalmanagement.system.repository.OwnerRepository;
import com.rentalmanagement.system.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final OwnerRepository ownerRepository;
    private final TenantRepository tenantRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return ownerRepository.findByEmail(email)
                .map(AppUserPrincipal::fromOwner)
                .map(UserDetails.class::cast)
                .or(() -> tenantRepository.findByEmail(email).map(AppUserPrincipal::fromTenant))
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    }
}
