package com.carddemo.auth.repository;

import com.carddemo.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for {@link User} records.
 *
 * <p>Replaces the VSAM {@code USRSEC} file I/O that the legacy programs
 * performed via {@code EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT}:
 * <ul>
 *   <li>{@code COSGN00C} - read by key on sign-on.</li>
 *   <li>{@code COUSR00C} - sequential browse (now {@code findAll(Pageable)}).</li>
 *   <li>{@code COUSR01C} - write (now {@code save}).</li>
 *   <li>{@code COUSR02C} - read + rewrite (now {@code save}).</li>
 *   <li>{@code COUSR03C} - delete (now {@code deleteById}).</li>
 * </ul>
 * The VSAM key {@code SEC-USR-ID} is the JPA primary key, so lookups use the
 * standard {@code findById}/{@code existsById} methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
