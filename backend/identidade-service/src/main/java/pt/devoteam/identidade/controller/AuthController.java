package pt.devoteam.identidade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.devoteam.identidade.dto.RegistoDTO;
import pt.devoteam.identidade.entity.Utilizador;
import pt.devoteam.identidade.security.JwtUtil;
import pt.devoteam.identidade.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") //
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint dinâmico de Login ligado à Base de Dados Única
     */
    @PostMapping("/login") //
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) { //
        String email = credentials.get("email"); //
        String password = credentials.get("password"); //

        return userService.autenticar(email, password)
                .map(utilizador -> {
                    String token = JwtUtil.generateToken(utilizador.getEmail(), utilizador.getRole());
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "email", utilizador.getEmail(),
                            "role", utilizador.getRole()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciais ou perfil inválidos."))); //
    }

    /**
     * Endpoint de Registo unificado para as 6 Roles da Plataforma
     */
    @PostMapping("/registar")
    public ResponseEntity<?> registar(@RequestBody RegistoDTO dto) {
        try {
            // Validação básica do payload enviado pelo Angular
            if (dto.getRole() == null || !dto.getRole().startsWith("ROLE_")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Perfil de acesso (Role) inválido ou ausente."));
            }

            Utilizador novoUser = userService.registar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "email", novoUser.getEmail(),
                    "role", novoUser.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao processar o registo: " + e.getMessage()));
        }
    }
}