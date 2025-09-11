package br.edu.ifba.saj.fwads.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
public class Gasto extends AbstractEntity {

    @Column(nullable = false)
    @NotBlank
    private String nomeGasto;

    @Column(nullable = false)
    @NotNull
    @Positive
    private BigDecimal valorGasto;

    @Column(nullable = false)
    @NotNull
    private LocalDate data;

    @Column(nullable = false)
    private boolean gastoFixo;

    @Column(nullable = false)
    @NotNull
    private UUID usuarioId;

    // Getters e Setters
    public String getNomeGasto() { return nomeGasto; }
    public void setNomeGasto(String nomeGasto) { this.nomeGasto = nomeGasto; }

    public BigDecimal getValorGasto() { return valorGasto; }
    public void setValorGasto(BigDecimal valorGasto) { this.valorGasto = valorGasto; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public boolean isGastoFixo() { return gastoFixo; }
    public void setGastoFixo(boolean gastoFixo) { this.gastoFixo = gastoFixo; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public String toString() {
        return "Gasto [id=" + getId() + ", nomeGasto=" + nomeGasto +
               ", valorGasto=" + valorGasto + ", data=" + data +
               ", gastoFixo=" + gastoFixo + ", usuarioId=" + usuarioId + "]";
    }
}
