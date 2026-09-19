package taskmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.CreateUserRequest;
import taskmanagement.user.UserEntity;
import taskmanagement.user.UserRepository;

@RestController
@RequestMapping("/api/accounts")
public class UserController {
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserController(UserRepository repository, PasswordEncoder encoder){
        this.repository = repository;
        this.encoder = encoder;
    }

    @PostMapping
    ResponseEntity<Void> registerUser(@Valid @RequestBody CreateUserRequest newUserDto){
        if (repository.existsByEmail(newUserDto.email())){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String encodedPassword = encoder.encode(newUserDto.password());

        UserEntity newUser = new UserEntity();
        newUser.setEmail(newUserDto.email());
        newUser.setPassword(encodedPassword);
        repository.save(newUser);

    return ResponseEntity.ok().build();

    }


}
