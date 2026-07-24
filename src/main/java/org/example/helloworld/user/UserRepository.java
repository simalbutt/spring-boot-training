package org.example.helloworld.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

//    @Query("select o from User o where o.username = :username ")
//    Optional<User> findByUsername1(@Param("username") String username);
//
//    @Query(
//            value = "SELECT * FROM users u WHERE u.username = ?1",
//            nativeQuery = true
//    )
//    Optional<User> findByUsername2(String username);
}
