package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.bus.EventBus;
import br.edu.ifba.saj.fwads.model.Receita;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CadReceitaController {

    @FXML private DatePicker cadReceitaData;
    @FXML private TextField cadReceitaValor;
    @FXML private ChoiceBox<String> choiceBoxCategoriaRec;

    private Service<Receita> serviceReceita = new Service<>(Receita.class);
    private Usuario usuarioLogado;

    private final List<String> categoriasReceita = Arrays.asList(
            "Salário", "Freelance", "Investimentos", "Outros"
    );

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    @FXML
    private void initialize() {
        choiceBoxCategoriaRec.getItems().addAll(categoriasReceita);
    }

    @FXML
    void salvarReceita(ActionEvent event) {
        try {
            String valorStr = cadReceitaValor.getText();
            LocalDate data = cadReceitaData.getValue();
            String categoria = choiceBoxCategoriaRec.getValue();

            if (categoria == null || valorStr == null || valorStr.isBlank() || data == null) {
                new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
                return;
            }
            if (usuarioLogado == null) {
                new Alert(AlertType.ERROR, "Erro crítico: Nenhum usuário logado.").showAndWait();
                return;
            }

            String normalized = valorStr.trim().replace(",", ".");
            BigDecimal valor = new BigDecimal(normalized);

            Receita novaReceita = new Receita();
            novaReceita.setNome(categoria);
            novaReceita.setValor(valor);
            novaReceita.setData(data);
            novaReceita.setUsuarioId(usuarioLogado.getId());
            serviceReceita.create(novaReceita);
            serviceReceita.clearCache();

            EventBus.getInstance().publish(new EventBus.Event("RECEITA_CADASTRADA", novaReceita));

            new Alert(AlertType.INFORMATION, "Receita na categoria '" + categoria + "' cadastrada com sucesso!").showAndWait();

            limparTela(null);

            if (cadReceitaValor.getScene() != null) {
                Stage stage = (Stage) cadReceitaValor.getScene().getWindow();
                if (stage != null && stage.getOwner() != null) {
                    stage.close();
                }
            }

        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Erro ao salvar receita.").showAndWait();
        }
    }

    @FXML
    void limparTela(ActionEvent event) {
        cadReceitaValor.setText("");
        cadReceitaData.setValue(null);
        choiceBoxCategoriaRec.setValue(null);
    }
}