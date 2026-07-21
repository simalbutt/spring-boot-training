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
        this.passwordEncoder = passwordEncoder;
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

    public User findOrCreateUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole("ROLE_REPORTER");
        }
        user.setToken(UUID.randomUUID().toString());
        return userRepository.save(user);
    }
}
