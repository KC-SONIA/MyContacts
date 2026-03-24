package com.mycontacts.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailDto {
    private Long id;
    
    @Email(message = "Email should be valid")
    private String email;
}
