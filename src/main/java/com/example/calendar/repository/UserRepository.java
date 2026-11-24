package com.example.calendar.repository;

import com.example.calendar.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmail(String email);

    @Query("""
            select u from User u
            where u.id <> :excludeUserId
              and (lower(u.email) like lower(concat('%', :keyword, '%'))
                   or lower(u.name) like lower(concat('%', :keyword, '%')))
            order by u.name
            """)
    List<User> searchByKeyword(@Param("keyword") String keyword, @Param("excludeUserId") Long excludeUserId, Pageable pageable);
}
