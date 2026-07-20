package org.example.helloworld.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Optional<user> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return User.withUsername(username).password(user.get().getPassword())
                .authorities("ROLE_" + user.get().getRole()).build();

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
