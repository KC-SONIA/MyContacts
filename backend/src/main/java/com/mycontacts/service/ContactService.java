package com.mycontacts.service;

import com.mycontacts.dto.*;
import com.mycontacts.entity.*;
import com.mycontacts.exception.DuplicateContactException;
import com.mycontacts.exception.ResourceNotFoundException;
import com.mycontacts.repository.ContactRepository;
import com.mycontacts.repository.PhoneNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for contact CRUD, search, soft-delete, and bin operations.
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final PhoneNumberRepository phoneNumberRepository;

    // ─────────────────────── CREATE ───────────────────────

    @Transactional
    public ContactResponse createContact(ContactRequest request, User user) {
        validateDuplicates(request, user, null);

        Contact contact = Contact.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .birthday(parseBirthday(request.getBirthday()))
                .isDeleted(false)
                .user(user)
                .build();

        // Map phone numbers
        List<PhoneNumber> phones = request.getPhoneNumbers().stream()
                .map(dto -> PhoneNumber.builder()
                        .phoneNumber(dto.getPhoneNumber().trim())
                        .contact(contact)
                        .build())
                .toList();
        contact.setPhoneNumbers(phones);

        // Map emails
        if (request.getEmails() != null) {
            List<Email> emails = request.getEmails().stream()
                    .filter(dto -> dto.getEmail() != null && !dto.getEmail().isBlank())
                    .map(dto -> Email.builder()
                            .email(dto.getEmail().trim())
                            .contact(contact)
                            .build())
                    .toList();
            contact.setEmails(emails);
        }

        return toResponse(contactRepository.save(contact));
    }

    // ─────────────────────── UPDATE ───────────────────────

    @Transactional
    public ContactResponse updateContact(Long contactId, ContactRequest request, User user) {
        Contact contact = findContactByIdAndUser(contactId, user);
        validateDuplicates(request, user, contactId);

        contact.setFirstName(request.getFirstName().trim());
        contact.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        contact.setBirthday(parseBirthday(request.getBirthday()));

        // Replace phone numbers
        contact.getPhoneNumbers().clear();
        List<PhoneNumber> phones = request.getPhoneNumbers().stream()
                .map(dto -> PhoneNumber.builder()
                        .phoneNumber(dto.getPhoneNumber().trim())
                        .contact(contact)
                        .build())
                .toList();
        contact.getPhoneNumbers().addAll(phones);

        // Replace emails
        contact.getEmails().clear();
        if (request.getEmails() != null) {
            List<Email> emails = request.getEmails().stream()
                    .filter(dto -> dto.getEmail() != null && !dto.getEmail().isBlank())
                    .map(dto -> Email.builder()
                            .email(dto.getEmail().trim())
                            .contact(contact)
                            .build())
                    .toList();
            contact.getEmails().addAll(emails);
        }

        return toResponse(contactRepository.save(contact));
    }

    // ─────────────────────── SOFT DELETE ───────────────────────

    @Transactional
    public void softDeleteContact(Long contactId, User user) {
        Contact contact = findContactByIdAndUser(contactId, user);
        contact.setIsDeleted(true);
        contactRepository.save(contact);
    }

    // ─────────────────────── LIST (PAGINATED) ───────────────────────

    public Page<ContactResponse> getContacts(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        return contactRepository.findByUserAndIsDeletedFalse(user, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────── SEARCH ───────────────────────

    public List<ContactResponse> searchContacts(User user, String query) {
        return contactRepository.searchContacts(user, query.trim()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────── BIN ───────────────────────

    public List<ContactResponse> getBinContacts(User user) {
        return contactRepository.findByUserAndIsDeletedTrue(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContactResponse restoreContact(Long contactId, User user) {
        Contact contact = findContactByIdAndUser(contactId, user);
        contact.setIsDeleted(false);
        return toResponse(contactRepository.save(contact));
    }

    @Transactional
    public void permanentlyDeleteContact(Long contactId, User user) {
        Contact contact = findContactByIdAndUser(contactId, user);
        contactRepository.delete(contact);
    }

    // ─────────────────────── HELPERS ───────────────────────

    private Contact findContactByIdAndUser(Long id, User user) {
        return contactRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }

    /**
     * Validates that no other contact for this user has the same name or phone number.
     * @param excludeId Contact ID to exclude from checks (for updates).
     */
    private void validateDuplicates(ContactRequest request, User user, Long excludeId) {
        // Check duplicate name
        boolean nameExists = contactRepository.existsByUserAndName(
                user, request.getFirstName().trim(),
                request.getLastName() != null ? request.getLastName().trim() : null,
                excludeId);
        if (nameExists) {
            throw new DuplicateContactException(
                    "Contact with this mobile number or name already exists");
        }

        // Check duplicate phone numbers
        for (PhoneNumberDto dto : request.getPhoneNumbers()) {
            phoneNumberRepository.findDuplicate(dto.getPhoneNumber().trim(), user.getId(), excludeId)
                    .ifPresent(existing -> {
                        throw new DuplicateContactException(
                                "Contact with this mobile number or name already exists");
                    });
        }
    }

    private LocalDate parseBirthday(String birthday) {
        if (birthday == null || birthday.isBlank()) {
            return null;
        }
        return LocalDate.parse(birthday);
    }

    /** Maps a Contact entity to a ContactResponse DTO. */
    private ContactResponse toResponse(Contact contact) {
        ContactResponse dto = new ContactResponse();
        dto.setId(contact.getId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setBirthday(contact.getBirthday());
        dto.setIsDeleted(contact.getIsDeleted());

        dto.setPhoneNumbers(contact.getPhoneNumbers().stream()
                .map(p -> {
                    PhoneNumberDto pd = new PhoneNumberDto();
                    pd.setId(p.getId());
                    pd.setPhoneNumber(p.getPhoneNumber());
                    return pd;
                }).collect(Collectors.toList()));

        dto.setEmails(contact.getEmails().stream()
                .map(e -> {
                    EmailDto ed = new EmailDto();
                    ed.setId(e.getId());
                    ed.setEmail(e.getEmail());
                    return ed;
                }).collect(Collectors.toList()));

        return dto;
    }
}
