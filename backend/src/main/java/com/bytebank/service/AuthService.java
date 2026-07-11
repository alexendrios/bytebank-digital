package com.bytebank.service;

import com.bytebank.dto.request.LoginRequest;
import com.bytebank.dto.request.RefreshTokenRequest;
import com.bytebank.dto.request.RegisterRequest;
import com.bytebank.dto.response.AuthResponse;
import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.exception.ConflictException;
import com.bytebank.repository.UsuarioRepository;
import com.bytebank.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("Já existe um usuário cadastrado com este e-mail");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.CLIENTE)
                .build();

        usuarioRepository.save(usuario);

        return new AuthResponse(
                jwtService.generateAccessToken(usuario),
                jwtService.generateRefreshToken(usuario),
                "Bearer"
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new AuthResponse(
                jwtService.generateAccessToken(usuario),
                jwtService.generateRefreshToken(usuario),
                "Bearer"
        );
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtService.isRefreshToken(token)) {
            throw new JwtException("Token informado não é um refresh token válido");
        }

        String email = jwtService.extractEmail(token);
        UserDetails usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (!jwtService.isValid(token, usuario)) {
            throw new JwtException("Refresh token inválido ou expirado");
        }

        return new AuthResponse(
                jwtService.generateAccessToken(usuario),
                jwtService.generateRefreshToken(usuario),
                "Bearer"
        );
    }
}
