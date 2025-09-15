package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.model.Gasto;
import br.edu.ifba.saj.fwads.model.Receita;
import br.edu.ifba.saj.fwads.model.RelatorioMensal;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.GastoService;
import br.edu.ifba.saj.fwads.service.ReceitaService;
import br.edu.ifba.saj.fwads.bus.EventBus;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SaldoController implements EventBus.EventListener {

    @FXML private Label lblSaldoAtual;
    @FXML private Label lblGastoPrevisto;
    @FXML private ComboBox<String> cbMesPrevisao;
    @FXML private ComboBox<Integer> cbAnoPrevisao;

    private Usuario usuarioLogado;
    private GastoService gastoService = new GastoService();
    private ReceitaService receitaService = new ReceitaService();

    private final List<String> meses = Arrays.asList(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    @FXML
    private void initialize() {
        EventBus.getInstance().subscribe(this);
    }

    @Override
    public void onEvent(EventBus.Event event) {
        if ("GASTO_CADASTRADO".equals(event.getType()) || "RECEITA_CADASTRADA".equals(event.getType())) {
            Platform.runLater(() -> {
                calcularSaldos();
            });
        }
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        inicializarComboBoxes();
        calcularSaldos();
    }

    private void inicializarComboBoxes() {
        cbMesPrevisao.getItems().clear();
        cbMesPrevisao.getItems().addAll(meses);
        cbMesPrevisao.setValue(meses.get(LocalDate.now().getMonthValue() - 1));
        cbMesPrevisao.valueProperty().addListener((obs, oldV, newV) -> calcularSaldos());

        int anoAtual = LocalDate.now().getYear();
        cbAnoPrevisao.getItems().clear();
        for (int ano = anoAtual; ano <= anoAtual + 5; ano++) {
            cbAnoPrevisao.getItems().add(ano);
        }
        cbAnoPrevisao.setValue(anoAtual);
        cbAnoPrevisao.valueProperty().addListener((obs, oldV, newV) -> calcularSaldos());
    }

    private void calcularSaldos() {
        if (usuarioLogado == null) return;
        gastoService.clearCache();
        receitaService.clearCache();
        List<Gasto> todosGastos = gastoService.findAll();
        List<Gasto> gastosDoUsuario = todosGastos.stream()
                .filter(g -> g.getUsuarioId().equals(usuarioLogado.getId()))
                .collect(Collectors.toList());

        List<Receita> todasReceitas = receitaService.findAll();
        List<Receita> receitasDoUsuario = todasReceitas.stream()
                .filter(r -> r.getUsuarioId().equals(usuarioLogado.getId()))
                .collect(Collectors.toList());

        RelatorioMensal relatorio = new RelatorioMensal(usuarioLogado.getId(), 0, 0);
        relatorio.calcularSaldoAteHoje(gastosDoUsuario, receitasDoUsuario);

        BigDecimal saldoAtual = relatorio.getSaldoAtual();
        lblSaldoAtual.setText("R$ " + saldoAtual.setScale(2, RoundingMode.HALF_UP).toString());

        Integer anoPrevisao = cbAnoPrevisao.getValue();
        String mesPrevisao = cbMesPrevisao.getValue();

        if (anoPrevisao == null || mesPrevisao == null) {
            lblGastoPrevisto.setText("R$ 0,00");
            return;
        }

        int mesNumero = meses.indexOf(mesPrevisao) + 1;
        BigDecimal gastosPrevistos = calcularGastosFixosPrevistos(anoPrevisao, mesNumero, gastosDoUsuario);
        lblGastoPrevisto.setText("R$ " + gastosPrevistos.setScale(2, RoundingMode.HALF_UP).toString());
    }

    private BigDecimal calcularGastosFixosPrevistos(int anoPrevisao, int mesPrevisao, List<Gasto> gastosDoUsuario) {
        BigDecimal totalGastosFixos = BigDecimal.ZERO;

        List<Gasto> gastosFixos = gastosDoUsuario.stream()
                .filter(Gasto::isGastoFixo)
                .collect(Collectors.toList());

        for (Gasto gasto : gastosFixos) {
            LocalDate dataGasto = gasto.getData();
            if (dataGasto.getYear() < anoPrevisao ||
                (dataGasto.getYear() == anoPrevisao && dataGasto.getMonthValue() <= mesPrevisao)) {
                totalGastosFixos = totalGastosFixos.add(gasto.getValorGasto());
            }
        }
        return totalGastosFixos;
    }
}