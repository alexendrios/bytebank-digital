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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_TENTATIVAS_FALHAS = 5;
    private static final long BLOQUEIO_MINUTOS = 15;

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

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (BadCredentialsException ex) {
            // Credenciais erradas: soma uma tentativa falha e, se atingir o
            // limite, bloqueia a conta temporariamente. Não revela ao
            // chamador se o e-mail existe ou não (a exceção original é
            // relançada tal como veio do AuthenticationManager).
            registrarTentativaFalha(request.email());
            throw ex;
        } catch (LockedException ex) {
            // Conta já bloqueada por tentativas anteriores: apenas propaga,
            // sem contar mais uma tentativa nem estender o bloqueio.
            throw ex;
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (usuario.getTentativasFalhas() > 0 || usuario.getBloqueadoAte() != null) {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
            usuarioRepository.save(usuario);
        }

        return new AuthResponse(
                jwtService.generateAccessToken(usuario),
                jwtService.generateRefreshToken(usuario),
                "Bearer"
        );
    }

    private void registrarTentativaFalha(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            int tentativas = usuario.getTentativasFalhas() + 1;
            usuario.setTentativasFalhas(tentativas);
            if (tentativas >= MAX_TENTATIVAS_FALHAS) {
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(BLOQUEIO_MINUTOS));
            }
            usuarioRepository.save(usuario);
        });
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
