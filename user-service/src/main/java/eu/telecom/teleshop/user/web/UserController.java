package eu.telecom.teleshop.user.web;

import eu.telecom.teleshop.user.model.User;
import eu.telecom.teleshop.user.repository.UserRepository;
import eu.telecom.teleshop.user.events.UserDeleted;
import eu.telecom.teleshop.user.service.UserEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserEventPublisher publisher;

    public UserController(UserRepository userRepository, UserEventPublisher publisher) {
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(saved);
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getAll() {
        Iterable<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        publisher.publishUserDeleted(new UserDeleted(id));
        return ResponseEntity.noContent().build();
    }
}
