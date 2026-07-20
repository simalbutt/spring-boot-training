package org.example.helloworld.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return org.springframework.security.core.userdetails.User.withUsername(username).password(user.get().getPassword())
                .authorities("ROLE_" + user.get().getRole()).build();

    }

    public User findOrCreateUser(String username, String role) {

        return userRepository.findByUsername(username)
                .orElseGet(() -> {

                    User newUser = new User();

                    newUser.setUsername(username);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(role.replace("ROLE_", ""));

                    return userRepository.save(newUser);
                });
    }

//    public  user generateToken(String username) {
//        user user1= userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException(username));
//        user1.setToken(UUID.randomUUID().toString());
//        return userRepository.save(user1);
//    }
//
//    public user findByToken(String token) {
//        Optional<user> user = userRepository.findByToken(token);
//        if(user.isEmpty()) {
//            throw new UsernameNotFoundException(token);
//        }
//        return user.get();
//    }

}
