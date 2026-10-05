package ir.av.dws.user.controller;

import ir.av.dws.user.dto.TokenResponse;
import ir.av.dws.user.dto.UserRequest;
import ir.av.dws.user.entity.User;
import ir.av.dws.user.service.JwtService;
import ir.av.dws.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService,
                          UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserRequest userRequest) {
        User login = userService.login(userRequest.username(), userRequest.password());
        String s = jwtService.generateToken(login);
        return ResponseEntity.ok().body(new TokenResponse(s, "bearer"));
    }
}