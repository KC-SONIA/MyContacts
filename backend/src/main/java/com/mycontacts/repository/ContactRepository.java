package com.mycontacts.repository;

import com.mycontacts.entity.Contact;
import com.mycontacts.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /** Find all active (non-deleted) contacts for a user, paginated. */
    Page<Contact> findByUserAndIsDeletedFalse(User user, Pageable pageable);

    /** Find all soft-deleted contacts for a user. */
    List<Contact> findByUserAndIsDeletedTrue(User user);

    /** Find an active contact by id and user. */
    Optional<Contact> findByIdAndUser(Long id, User user);

    /** Search contacts by name or phone number (case-insensitive, partial match). */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.phoneNumbers p " +
           "WHERE c.user = :user AND c.isDeleted = false " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR p.phoneNumber LIKE CONCAT('%', :query, '%'))")
    List<Contact> searchContacts(@Param("user") User user, @Param("query") String query);

    /** Check if a contact with the same first+last name already exists for this user. */
    @Query("SELECT COUNT(c) > 0 FROM Contact c " +
           "WHERE c.user = :user AND c.isDeleted = false " +
           "AND LOWER(c.firstName) = LOWER(:firstName) " +
           "AND (LOWER(c.lastName) = LOWER(:lastName) OR (c.lastName IS NULL AND :lastName IS NULL)) " +
           "AND (:excludeId IS NULL OR c.id <> :excludeId)")
    boolean existsByUserAndName(@Param("user") User user,
                                @Param("firstName") String firstName,
                                @Param("lastName") String lastName,
                                @Param("excludeId") Long excludeId);
}
