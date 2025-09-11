package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.model.Gasto;
import br.edu.ifba.saj.fwads.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CadGastoController {

    @FXML
    private DatePicker cadGastoData;

    @FXML
    private TextField cadGastoDescricao;

    @FXML
    private TextField cadGastoValor;

    @FXML
    private ChoiceBox<String> choiceBoxTipo; // "Fixo" ou "Normal"

    private Service<Gasto> serviceGasto = new Service<>(Gasto.class);

    private ListGastoController listGastoController;

    @FXML
    private void initialize() {
        choiceBoxTipo.getItems().addAll("Fixo", "Normal");
    }

    @FXML
    void salvarGasto(ActionEvent event) {
        try {
            String descricao = cadGastoDescricao.getText();
            String valorStr = cadGastoValor.getText();
            LocalDate data = cadGastoData.getValue();
            String tipo = choiceBoxTipo.getValue();

            
            if (descricao == null || descricao.isBlank() ||
                valorStr == null || valorStr.isBlank() ||
                data == null || tipo == null) {
                new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
                return;
            }

            BigDecimal valor = new BigDecimal(valorStr);
            boolean gastoFixo = tipo.equalsIgnoreCase("Fixo");

            
            Gasto novoGasto = new Gasto();
            novoGasto.setNomeGasto(descricao);
            novoGasto.setValorGasto(valor);
            novoGasto.setData(data);
            novoGasto.setGastoFixo(gastoFixo);
            novoGasto.setUsuarioId(UUID.randomUUID()); 

            serviceGasto.create(novoGasto);

            new Alert(AlertType.INFORMATION,
                    "Gasto '" + descricao + "' cadastrado com sucesso!")
                    .showAndWait();

            limparTela(event);

        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
        }
    }

    @FXML
    void limparTela(ActionEvent event) {
        cadGastoDescricao.setText("");
        cadGastoValor.setText("");
        cadGastoData.setValue(null);
        choiceBoxTipo.setValue(null);
    }

    public void setListGastoController(ListGastoController listGastoController) {
        this.listGastoController = listGastoController;
    }
}
