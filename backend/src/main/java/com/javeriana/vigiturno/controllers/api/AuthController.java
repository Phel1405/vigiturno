package com.javeriana.vigiturno.controllers.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.LoginRequest;
import com.javeriana.vigiturno.dtos.api.ApiDtos.LoginResponse;
import com.javeriana.vigiturno.dtos.api.ApiDtos.GoogleLoginRequest;
import com.javeriana.vigiturno.dtos.api.ApiDtos.UsuarioRequest;
import com.javeriana.vigiturno.dtos.api.ApiDtos.UsuarioDto;
import com.javeriana.vigiturno.dtos.api.ApiMapper;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.repositories.UsuarioRepository;
import com.javeriana.vigiturno.security.JwtTokenProvider;
import com.javeriana.vigiturno.services.GoogleTokenService;
import com.javeriana.vigiturno.services.GoogleTokenService.GoogleUserInfo;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElse(null);
        
        if (usuario == null || !usuario.getActivo() || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales incorrectas o cuenta deshabilitada");
        }

        String token = jwtTokenProvider.generateToken(usuario);
        LoginResponse response = new LoginResponse(
                token,
                usuario.getCorreo(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioRequest request) {
        if (usuarioRepository.findByCorreo(request.correo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El correo electrónico ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setCorreo(request.correo());
        
        String password = request.password() == null || request.password().isBlank() ? "123456" : request.password();
        usuario.setPassword(passwordEncoder.encode(password));
        
        usuario.setRol(request.rol() != null ? request.rol() : RolNombre.DOCENTE);
        usuario.setActivo(true);

        Usuario saved = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toDto(saved));
    }

}
