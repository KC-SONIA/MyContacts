package com.mycontacts.repository;

import com.mycontacts.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {

    /** Check if a phone number exists for any contact belonging to this user (excluding a given contact). */
    @Query("SELECT p FROM PhoneNumber p " +
           "WHERE p.phoneNumber = :phone " +
           "AND p.contact.user.id = :userId " +
           "AND (:excludeContactId IS NULL OR p.contact.id <> :excludeContactId)")
    Optional<PhoneNumber> findDuplicate(@Param("phone") String phone,
                                        @Param("userId") Long userId,
                                        @Param("excludeContactId") Long excludeContactId);
}
