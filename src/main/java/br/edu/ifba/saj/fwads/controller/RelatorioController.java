package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.model.Gasto;
import br.edu.ifba.saj.fwads.model.Receita;
import br.edu.ifba.saj.fwads.model.RelatorioMensal;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.GastoService;
import br.edu.ifba.saj.fwads.service.ReceitaService;
import br.edu.ifba.saj.fwads.bus.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.text.NumberFormat;

public class RelatorioController implements EventBus.EventListener {

    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbPeriodo;
    @FXML private ComboBox<Integer> cbAno;
    @FXML private ComboBox<String> cbMes;
    @FXML private TableView<Map.Entry<String, BigDecimal>> tblRelatorio;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colCategoria;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, BigDecimal> colValor;
    @FXML private Label lblTotal;

    private Usuario usuarioLogado;
    private GastoService gastoService = new GastoService();
    private ReceitaService receitaService = new ReceitaService();

    private final List<String> meses = Arrays.asList(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    @FXML
    private void initialize() {
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colValor.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getValue()));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        colValor.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nf.format(item));
            }
        });

        cbTipo.getItems().addAll("Gasto", "Gasto Fixo", "Receita");
        cbTipo.setValue("Gasto");

        cbPeriodo.getItems().addAll("Mensal", "Anual");
        cbPeriodo.setValue("Mensal");
        cbPeriodo.valueProperty().addListener((obs, oldV, newV) -> {
            cbMes.setDisable("Anual".equals(newV));
            if ("Anual".equals(newV)) cbMes.setValue(null);
        });
        
        // Registrar este controlador como listener de eventos
        EventBus.getInstance().subscribe(this);
    }

    @Override
    public void onEvent(EventBus.Event event) {
        if ("GASTO_CADASTRADO".equals(event.getType()) || "RECEITA_CADASTRADA".equals(event.getType())) {
            Platform.runLater(() -> {
                // Se já temos um relatório gerado, atualizá-lo automaticamente
                if (usuarioLogado != null && cbTipo.getValue() != null && cbAno.getValue() != null) {
                    gerarRelatorio();
                }
            });
        }
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        inicializarComboboxes();
    }

    private void inicializarComboboxes() {
        int currentYear = LocalDate.now().getYear();
        cbAno.getItems().clear();
        for (int year = currentYear - 5; year <= currentYear + 1; year++) {
            cbAno.getItems().add(year);
        }
        cbAno.setValue(currentYear);

        cbMes.getItems().clear();
        cbMes.getItems().addAll(meses);
        cbMes.setValue(meses.get(LocalDate.now().getMonthValue() - 1));
    }

    @FXML
    private void gerarRelatorio() {
        if (usuarioLogado == null) return;

        Map<String, BigDecimal> dados = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        String tipo = cbTipo.getValue();
        String periodo = cbPeriodo.getValue();
        Integer ano = cbAno.getValue();
        String mes = cbMes.getValue();
        int mesNumero = cbMes.getSelectionModel().getSelectedIndex() + 1;

        if (tipo == null || periodo == null || ano == null) return;
        if ("Mensal".equals(periodo) && mes == null) return;
        gastoService.clearCache();
        receitaService.clearCache();

        if ("Gasto".equals(tipo) || "Gasto Fixo".equals(tipo)) {
            boolean apenasFixos = "Gasto Fixo".equals(tipo);
            List<Gasto> todosGastos = gastoService.findAll();
            List<Gasto> gastos = todosGastos.stream()
                    .filter(g -> g.getUsuarioId().equals(usuarioLogado.getId()))
                    .collect(Collectors.toList());

            if ("Mensal".equals(periodo)) {
                gastos = gastos.stream()
                        .filter(g -> g.getData().getYear() == ano && g.getData().getMonthValue() == mesNumero)
                        .collect(Collectors.toList());
            } else {
                gastos = gastos.stream()
                        .filter(g -> g.getData().getYear() == ano)
                        .collect(Collectors.toList());
            }

            gastos = gastos.stream()
                    .filter(g -> g.isGastoFixo() == apenasFixos)
                    .collect(Collectors.toList());

            for (Gasto gasto : gastos) {
                String categoria = gasto.getNomeGasto();
                BigDecimal atual = dados.getOrDefault(categoria, BigDecimal.ZERO);
                BigDecimal novo = atual.add(gasto.getValorGasto());
                dados.put(categoria, novo);
                total = total.add(gasto.getValorGasto());
            }

        } else if ("Receita".equals(tipo)) {
            List<Receita> todasReceitas = receitaService.findAll();
            List<Receita> receitas = todasReceitas.stream()
                    .filter(r -> r.getUsuarioId().equals(usuarioLogado.getId()))
                    .collect(Collectors.toList());

            if ("Mensal".equals(periodo)) {
                receitas = receitas.stream()
                        .filter(r -> r.getData().getYear() == ano && r.getData().getMonthValue() == mesNumero)
                        .collect(Collectors.toList());
            } else {
                receitas = receitas.stream()
                        .filter(r -> r.getData().getYear() == ano)
                        .collect(Collectors.toList());
            }

            for (Receita receita : receitas) {
                String categoria = receita.getNome();
                BigDecimal atual = dados.getOrDefault(categoria, BigDecimal.ZERO);
                BigDecimal novo = atual.add(receita.getValor());
                dados.put(categoria, novo);
                total = total.add(receita.getValor());
            }
        }

        ObservableList<Map.Entry<String, BigDecimal>> items = FXCollections.observableArrayList(dados.entrySet());
        tblRelatorio.setItems(items);

        lblTotal.setText("Total: " + NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(total.setScale(2, RoundingMode.HALF_UP)));
    }
}