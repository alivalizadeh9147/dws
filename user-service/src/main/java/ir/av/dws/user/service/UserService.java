package ir.av.dws.user.service;

import ir.av.dws.user.entity.User;
import ir.av.dws.user.exception.UserNotFoundException;
import ir.av.dws.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.repository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(@NotNull String username, @NotNull String password) {
        Optional<User> byUsername = repository.findByUsername(username);
        if (byUsername.isEmpty()) {
            throw new UserNotFoundException("User not found with username " + username);
        }
        User user = byUsername.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Password mismatch");
        }
        return user;
    }

    @Transactional
    public void register(String fullName, String username, String password) {
        password = passwordEncoder.encode(password);
        User user = User.builder()
                .fullName(fullName)
                .username(username)
                .password(password)
                .id(UUID.randomUUID())
                .build();
        repository.save(user);
    }
}
