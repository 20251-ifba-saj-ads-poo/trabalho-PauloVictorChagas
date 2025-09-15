package br.edu.ifba.saj.fwads.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RelatorioMensal {

    private UUID usuarioId;
    private int mes;
    private int ano;
    private BigDecimal totalGastosFixos = BigDecimal.ZERO;
    private BigDecimal totalGastosNormais = BigDecimal.ZERO;
    private BigDecimal totalReceitas = BigDecimal.ZERO;
    private BigDecimal totalGeral = BigDecimal.ZERO;
    private BigDecimal percentualGastosFixos = BigDecimal.ZERO;
    private BigDecimal percentualGastosNormais = BigDecimal.ZERO;
    private BigDecimal percentualReceitas = BigDecimal.ZERO;
    private BigDecimal totalReceitasAcumulado = BigDecimal.ZERO;
    private BigDecimal totalGastosAcumulado = BigDecimal.ZERO;
    private BigDecimal saldoAtual = BigDecimal.ZERO;

    public RelatorioMensal(UUID usuarioId, int mes, int ano) {
        this.usuarioId = usuarioId;
        this.mes = mes;
        this.ano = ano;
    }

    public void calcularSaldoAteHoje(List<Gasto> todosGastos, List<Receita> todasReceitas) {
        this.totalReceitasAcumulado = todasReceitas.stream()
                .filter(r -> !r.getData().isAfter(LocalDate.now()))
                .map(Receita::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalGastosAcumulado = todosGastos.stream()
                .filter(g -> !g.getData().isAfter(LocalDate.now()))
                .map(Gasto::getValorGasto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.saldoAtual = this.totalReceitasAcumulado.subtract(this.totalGastosAcumulado);
    }
    

    public BigDecimal getTotalReceitasAcumulado() { return totalReceitasAcumulado; }
    public BigDecimal getTotalGastosAcumulado() { return totalGastosAcumulado; }
    public BigDecimal getSaldoAtual() { return saldoAtual; }

    public void calcularRelatorio(List<Gasto> gastos, List<Receita> receitas) {
        List<Gasto> gastosDoMes = gastos.stream()
                .filter(g -> g.getUsuarioId().equals(usuarioId) &&
                               g.getData().getMonthValue() == mes &&
                               g.getData().getYear() == ano)
                .collect(Collectors.toList());

        List<Receita> receitasDoMes = receitas.stream()
                .filter(r -> r.getUsuarioId().equals(usuarioId) &&
                               r.getData().getMonthValue() == mes &&
                               r.getData().getYear() == ano)
                .collect(Collectors.toList());

        totalGastosFixos = gastosDoMes.stream()
                .filter(Gasto::isGastoFixo)
                .map(Gasto::getValorGasto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalGastosNormais = gastosDoMes.stream()
                .filter(g -> !g.isGastoFixo())
                .map(Gasto::getValorGasto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalReceitas = receitasDoMes.stream()
                .map(Receita::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalGeral = totalReceitas.subtract(totalGastosFixos).subtract(totalGastosNormais);

        calcularPercentuais();
    }

    private void calcularPercentuais() {
        BigDecimal totalMovimentacao = totalReceitas.add(totalGastosFixos).add(totalGastosNormais);
        if (totalMovimentacao.compareTo(BigDecimal.ZERO) > 0) {
            percentualReceitas = totalReceitas.divide(totalMovimentacao, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            percentualGastosFixos = totalGastosFixos.divide(totalMovimentacao, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            percentualGastosNormais = totalGastosNormais.divide(totalMovimentacao, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
    }

    // Getters 
    public UUID getUsuarioId() { return usuarioId; }
    public int getMes() { return mes; }
    public int getAno() { return ano; }
    public BigDecimal getTotalGastosFixos() { return totalGastosFixos; }
    public BigDecimal getTotalGastosNormais() { return totalGastosNormais; }
    public BigDecimal getTotalReceitas() { return totalReceitas; }
    public BigDecimal getTotalGeral() { return totalGeral; }
    public BigDecimal getPercentualGastosFixos() { return percentualGastosFixos; }
    public BigDecimal getPercentualGastosNormais() { return percentualGastosNormais; }
    public BigDecimal getPercentualReceitas() { return percentualReceitas; }
}