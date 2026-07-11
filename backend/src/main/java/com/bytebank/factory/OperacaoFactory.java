package com.bytebank.factory;

import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.exception.BusinessException;
import com.bytebank.strategy.OperacaoStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fábrica (GoF Factory Method) que resolve dinamicamente a
 * {@link OperacaoStrategy} correta a partir do {@link TipoMovimentacao}
 * solicitado. O Spring injeta automaticamente todos os beans que
 * implementam {@link OperacaoStrategy}; a fábrica os indexa por tipo.
 */
@Component
public class OperacaoFactory {

    private final Map<TipoMovimentacao, OperacaoStrategy> estrategias;

    public OperacaoFactory(List<OperacaoStrategy> strategies) {
        this.estrategias = strategies.stream()
                .collect(Collectors.toMap(OperacaoStrategy::getTipo, Function.identity()));
    }

    public OperacaoStrategy resolver(TipoMovimentacao tipo) {
        OperacaoStrategy strategy = estrategias.get(tipo);
        if (strategy == null) {
            throw new BusinessException("Tipo de operação não suportado: " + tipo);
        }
        return strategy;
    }
}
