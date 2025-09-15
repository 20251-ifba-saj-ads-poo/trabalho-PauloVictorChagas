package br.edu.ifba.saj.fwads.service;

import br.edu.ifba.saj.fwads.model.Gasto;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;

public class GastoService extends Service<Gasto> {

    public GastoService() {
        super(Gasto.class);
    }

    public List<Gasto> findByUsuarioId(UUID usuarioId) {
        Map<String, Object> params = new HashMap<>();
        params.put("usuarioId", usuarioId);
        return super.findByMap(params);
    }
}