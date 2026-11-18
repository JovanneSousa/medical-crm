package com.omnimed.crm.channel;

import com.omnimed.crm.model.CanalTipo;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Roteador polimórfico de canais omnichannel.
 * Resolve dinamicamente o {@link CanalComunicacao} correspondente ao {@link CanalTipo},
 * eliminando estruturas condicionais (if/else ou switch) espalhadas pelos serviços e controladores.
 */
@Service
public class DespachanteCanalService {

    private final Map<CanalTipo, CanalComunicacao> canaisPorTipo;

    public DespachanteCanalService(List<CanalComunicacao> canais) {
        this.canaisPorTipo = new EnumMap<>(CanalTipo.class);
        for (CanalComunicacao canal : canais) {
            this.canaisPorTipo.put(canal.getTipoCanal(), canal);
        }
    }

    public CanalComunicacao obterCanal(CanalTipo tipo) {
        CanalComunicacao canal = canaisPorTipo.get(tipo);
        if (canal == null) {
            throw new IllegalArgumentException("Canal de comunicação não suportado: " + tipo);
        }
        return canal;
    }

    public boolean enviarMensagem(CanalTipo tipo, String destinatarioId, String texto) {
        return obterCanal(tipo).enviarMensagem(destinatarioId, texto);
    }
}
