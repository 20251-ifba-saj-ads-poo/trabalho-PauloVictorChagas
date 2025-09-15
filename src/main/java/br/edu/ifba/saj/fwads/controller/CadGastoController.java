package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.bus.EventBus;
import br.edu.ifba.saj.fwads.model.Gasto;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CadGastoController {

    @FXML private DatePicker cadGastoData;
    @FXML private TextField cadGastoValor;
    @FXML private ChoiceBox<String> choiceBoxTipo;
    @FXML private ChoiceBox<String> choiceBoxCategoriaGasto;

    private Service<Gasto> serviceGasto = new Service<>(Gasto.class);
    private Usuario usuarioLogado;

    private final List<String> categoriasGasto = Arrays.asList(
            "Alimentação", "Transporte", "Moradia", "Saúde", "Educação", "Lazer", "Outros"
    );

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    @FXML
    private void initialize() {
        choiceBoxTipo.getItems().addAll("Fixo", "Normal");
        choiceBoxCategoriaGasto.getItems().addAll(categoriasGasto);
    }

    @FXML
    void salvarGasto(ActionEvent event) {
        try {
            String valorStr = cadGastoValor.getText();
            LocalDate data = cadGastoData.getValue();
            String tipo = choiceBoxTipo.getValue();
            String categoria = choiceBoxCategoriaGasto.getValue();

            if (categoria == null || valorStr == null || valorStr.isBlank() || data == null || tipo == null) {
                new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
                return;
            }
            if (usuarioLogado == null) {
                new Alert(AlertType.ERROR, "Erro crítico: Nenhum usuário logado.").showAndWait();
                return;
            }

            String normalized = valorStr.trim().replace(",", ".");
            BigDecimal valor = new BigDecimal(normalized);
            boolean gastoFixo = tipo.equalsIgnoreCase("Fixo");

            Gasto novoGasto = new Gasto();
            novoGasto.setNomeGasto(categoria);
            novoGasto.setValorGasto(valor);
            novoGasto.setData(data);
            novoGasto.setGastoFixo(gastoFixo);
            novoGasto.setUsuarioId(usuarioLogado.getId());
            serviceGasto.create(novoGasto);
            serviceGasto.clearCache();

            EventBus.getInstance().publish(new EventBus.Event("GASTO_CADASTRADO", novoGasto));

            new Alert(AlertType.INFORMATION, "Gasto na categoria '" + categoria + "' cadastrado com sucesso!").showAndWait();

            limparTela(null);

            if (cadGastoValor.getScene() != null) {
                Stage stage = (Stage) cadGastoValor.getScene().getWindow();
                if (stage != null && stage.getOwner() != null) {
                    stage.close();
                }
            }

        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Erro ao salvar gasto.").showAndWait();
        }
    }

    @FXML
    void limparTela(ActionEvent event) {
        cadGastoValor.setText("");
        cadGastoData.setValue(null);
        choiceBoxTipo.setValue(null);
        choiceBoxCategoriaGasto.setValue(null);
    }
}