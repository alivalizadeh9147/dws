package ir.av.dws.user.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import ir.av.dws.user.dto.TokenResponse;
import ir.av.dws.user.dto.UserRequest;
import ir.av.dws.user.entity.User;
import ir.av.dws.user.service.JwtService;
import ir.av.dws.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService,
                          UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    schema = @Schema(implementation = UserRequest.class, requiredMode = Schema.RequiredMode.REQUIRED)
            )
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@ModelAttribute UserRequest request) {
        User login = userService.login(request.username(), request.password());
        String s = jwtService.generateToken(login.getUsername());
        return ResponseEntity.ok().body(new TokenResponse(s, "bearer"));
    }
}