package com.rentalmanagement.system.dto.request;

import com.rentalmanagement.system.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyRequest {

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotNull
    private PropertyType type;

    @NotNull
    @Positive
    private BigDecimal rentAmount;
}
