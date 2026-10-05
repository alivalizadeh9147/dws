package ir.av.dws.user.controller;

import ir.av.dws.user.dto.RegisterRequest;
import ir.av.dws.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/public/users")
@RestController
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> registerUser(@ModelAttribute RegisterRequest request) {
        userService.register(request.fullName(), request.username(), request.password());
        return ResponseEntity.ok().build();
    }
}

