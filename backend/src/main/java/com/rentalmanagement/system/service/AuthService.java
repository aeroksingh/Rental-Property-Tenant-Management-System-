package com.rentalmanagement.system.service;

import com.rentalmanagement.system.dto.request.LoginRequest;
import com.rentalmanagement.system.dto.request.RegisterRequest;
import com.rentalmanagement.system.dto.response.JwtResponse;
import com.rentalmanagement.system.entity.Owner;
import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.enums.Role;
import com.rentalmanagement.system.exception.BadRequestException;
import com.rentalmanagement.system.repository.OwnerRepository;
import com.rentalmanagement.system.repository.TenantRepository;
import com.rentalmanagement.system.security.AppUserPrincipal;
import com.rentalmanagement.system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OwnerRepository ownerRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public JwtResponse register(RegisterRequest request) {
        boolean emailTaken = ownerRepository.existsByEmail(request.getEmail())
                || tenantRepository.existsByEmail(request.getEmail());

        if (emailTaken) {
            throw new BadRequestException("An account with this email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if (request.getRole() == Role.OWNER) {
            Owner owner = Owner.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .phone(request.getPhone())
                    .role(Role.OWNER)
                    .build();
            owner = ownerRepository.save(owner);

            String token = jwtUtil.generateToken(AppUserPrincipal.fromOwner(owner));
            return JwtResponse.builder()
                    .token(token)
                    .userId(owner.getId())
                    .name(owner.getName())
                    .email(owner.getEmail())
                    .role(owner.getRole())
                    .build();
        } else {
            Tenant tenant = Tenant.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .phone(request.getPhone())
                    .role(Role.TENANT)
                    .build();
            tenant = tenantRepository.save(tenant);

            String token = jwtUtil.generateToken(AppUserPrincipal.fromTenant(tenant));
            return JwtResponse.builder()
                    .token(token)
                    .userId(tenant.getId())
                    .name(tenant.getName())
                    .email(tenant.getEmail())
                    .role(tenant.getRole())
                    .build();
        }
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // Authentication succeeded -> look the user up to build the response/token
        return ownerRepository.findByEmail(request.getEmail())
                .map(owner -> JwtResponse.builder()
                        .token(jwtUtil.generateToken(AppUserPrincipal.fromOwner(owner)))
                        .userId(owner.getId())
                        .name(owner.getName())
                        .email(owner.getEmail())
                        .role(owner.getRole())
                        .build())
                .orElseGet(() -> tenantRepository.findByEmail(request.getEmail())
                        .map(tenant -> JwtResponse.builder()
                                .token(jwtUtil.generateToken(AppUserPrincipal.fromTenant(tenant)))
                                .userId(tenant.getId())
                                .name(tenant.getName())
                                .email(tenant.getEmail())
                                .role(tenant.getRole())
                                .build())
                        .orElseThrow(() -> new BadRequestException("User not found")));
    }
}
