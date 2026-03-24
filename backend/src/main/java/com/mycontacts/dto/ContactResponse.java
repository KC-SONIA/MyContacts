package com.mycontacts.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ContactResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Boolean isDeleted;
    private List<PhoneNumberDto> phoneNumbers;
    private List<EmailDto> emails;
}
