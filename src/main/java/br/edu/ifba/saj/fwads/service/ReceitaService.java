package br.edu.ifba.saj.fwads.service;

import br.edu.ifba.saj.fwads.model.Receita;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;

public class ReceitaService extends Service<Receita> {

    public ReceitaService() {
        super(Receita.class);
    }

    public List<Receita> findByUsuarioId(UUID usuarioId) {
        Map<String, Object> params = new HashMap<>();
        params.put("usuarioId", usuarioId);
        return super.findByMap(params);
    }
}