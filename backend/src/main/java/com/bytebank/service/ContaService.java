package com.bytebank.service;

import com.bytebank.dto.request.ContaRequest;
import com.bytebank.dto.response.ContaResponse;
import com.bytebank.entity.Conta;
import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.exception.ResourceNotFoundException;
import com.bytebank.mapper.ContaMapper;
import com.bytebank.repository.ContaRepository;
import com.bytebank.security.SecurityUtils;
import com.bytebank.util.NumeroContaGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Regras de negócio de contas. Endpoints são acessíveis tanto por ADMIN
 * quanto por CLIENTE, mas o CLIENTE só pode enxergar/operar as próprias
 * contas — regra reforçada aqui, pois depende do dono do recurso em
 * runtime (não é possível expressar isso apenas com {@code hasRole}).
 */
@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final UsuarioService usuarioService;
    private final ContaMapper contaMapper;

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        Usuario usuarioLogado = SecurityUtils.getUsuarioAutenticado();

        List<Conta> contas = usuarioLogado.getPerfil() == Perfil.ADMIN
                ? contaRepository.findAll()
                : contaRepository.findByUsuarioId(usuarioLogado.getId());

        return contas.stream().map(contaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(UUID id) {
        Conta conta = buscarEntidadePorId(id);
        validarPropriedade(conta);
        return contaMapper.toResponse(conta);
    }

    @Transactional(readOnly = true)
    public Conta buscarEntidadePorId(UUID id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Usuario titular = usuarioService.buscarEntidadePorId(request.usuarioId());

        String numero;
        do {
            numero = NumeroContaGenerator.gerar();
        } while (contaRepository.existsByNumero(numero));

        Conta conta = Conta.builder()
                .numero(numero)
                .agencia(request.agencia())
                .usuario(titular)
                .build();

        return contaMapper.toResponse(contaRepository.save(conta));
    }

    @Transactional
    public ContaResponse atualizar(UUID id, ContaRequest request) {
        Conta conta = buscarEntidadePorId(id);
        validarPropriedade(conta);

        if (!conta.getUsuario().getId().equals(request.usuarioId())) {
            Usuario novoTitular = usuarioService.buscarEntidadePorId(request.usuarioId());
            conta.setUsuario(novoTitular);
        }
        conta.setAgencia(request.agencia());

        return contaMapper.toResponse(contaRepository.save(conta));
    }

    @Transactional
    public void remover(UUID id) {
        Conta conta = buscarEntidadePorId(id);
        validarPropriedade(conta);
        contaRepository.delete(conta);
    }

    /**
     * Garante que o usuário autenticado é ADMIN ou o dono da conta.
     */
    void validarPropriedade(Conta conta) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioAutenticado();
        boolean admin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        boolean dono = conta.getUsuario().getId().equals(usuarioLogado.getId());

        if (!admin && !dono) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta conta");
        }
    }
}
