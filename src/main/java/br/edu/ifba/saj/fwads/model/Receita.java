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
public class Receita extends AbstractEntity {

    @Column(nullable = false)
    @NotBlank
    private String nome;

    @Column(nullable = false)
    @NotNull
    @Positive
    private BigDecimal valor;

    @Column(nullable = false)
    @NotNull
    private LocalDate data;

    @Column(nullable = false)
    @NotNull
    private UUID usuarioId;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public String toString() {
        return "Receita [id=" + getId() + ", nome=" + nome + 
               ", valor=" + valor + ", data=" + data + 
               ", usuarioId=" + usuarioId + "]";
    }
}