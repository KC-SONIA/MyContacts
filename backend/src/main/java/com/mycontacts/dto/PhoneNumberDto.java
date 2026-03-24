package com.mycontacts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PhoneNumberDto {
    private Long id;
    
    @NotBlank(message = "Phone number cannot be empty")
    @Size(min = 7, max = 20, message = "Phone number must be between 7 and 20 characters")
    private String phoneNumber;
}
