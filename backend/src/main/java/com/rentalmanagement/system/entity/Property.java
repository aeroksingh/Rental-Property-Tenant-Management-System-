package com.rentalmanagement.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rentalmanagement.system.enums.PropertyType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * NOTE on cardinality: modeled as one Property -> many Tenants so multi-unit
 * / historical-tenant support can be added later without a schema rework.
 * For now, business rules (see PropertyService) enforce at most one ACTIVE
 * tenant per property, which gives you today's one-to-one behavior.
 */
@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType type;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal rentAmount;

    @Builder.Default
    @Column(nullable = false)
    private boolean isOccupied = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private Owner owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    @JsonIgnore
    private List<Tenant> tenants = new ArrayList<>();
}
