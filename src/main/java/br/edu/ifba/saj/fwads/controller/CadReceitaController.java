package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.model.Receita;
import br.edu.ifba.saj.fwads.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CadReceitaController {

    @FXML
    private DatePicker cadReceitaData;

    @FXML
    private TextField cadReceitaDescricao;

    @FXML
    private TextField cadReceitaValor;

    private Service<Receita> serviceReceita = new Service<>(Receita.class);

    private ListReceitaController listReceitaController;


    @FXML
    void salvarReceita(ActionEvent event) {
        try {
            String descricao = cadReceitaDescricao.getText();
            String valorStr = cadReceitaValor.getText();
            LocalDate data = cadReceitaData.getValue();

            if (descricao == null || descricao.isBlank() ||
                valorStr == null || valorStr.isBlank() ||
                data == null) {
                new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
                return;
            }

            BigDecimal valor = new BigDecimal(valorStr);

            
            Receita novaReceita = new Receita();
            novaReceita.setNome(descricao);
            novaReceita.setValor(valor);
            novaReceita.setData(data);
            novaReceita.setUsuarioId(UUID.randomUUID());
            serviceReceita.create(novaReceita);

            new Alert(AlertType.INFORMATION,
                    "Receita '" + descricao + "' cadastrada com sucesso!")
                    .showAndWait();

            limparTela(event);

        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
        }
    }

    @FXML
    void limparTela(ActionEvent event) {
        cadReceitaDescricao.setText("");
        cadReceitaValor.setText("");
        cadReceitaData.setValue(null);
    }

    public void setListReceitaController(ListReceitaController listReceitaController) {
        this.listReceitaController = listReceitaController;
    }
}
