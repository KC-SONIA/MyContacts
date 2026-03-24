package com.mycontacts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ContactRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotEmpty(message = "At least one phone number is required")
    private List<PhoneNumberDto> phoneNumbers;

    private List<EmailDto> emails;

    /** Format: yyyy-MM-dd */
    private String birthday;
}
