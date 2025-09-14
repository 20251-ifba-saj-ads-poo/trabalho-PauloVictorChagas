package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.model.Gasto;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.Service;
import br.edu.ifba.saj.fwads.bus.EventBus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CadGastoController {

    @FXML
    private DatePicker cadGastoData;
    @FXML
    private TextField cadGastoDescricao;
    @FXML
    private TextField cadGastoValor;
    @FXML
    private ChoiceBox<String> choiceBoxTipo;

    private Service<Gasto> serviceGasto = new Service<>(Gasto.class);
    private Usuario usuarioLogado;

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        System.out.println("Usuário logado definido no CadGastoController: " + usuarioLogado);
    }

    @FXML
    private void initialize() {
        choiceBoxTipo.getItems().addAll("Fixo", "Normal");
        System.out.println("CadGastoController inicializado");
    }

    @FXML
    void salvarGasto(ActionEvent event) {
        try {
            String descricao = cadGastoDescricao.getText();
            String valorStr = cadGastoValor.getText();
            LocalDate data = cadGastoData.getValue();
            String tipo = choiceBoxTipo.getValue();

            if (descricao == null || descricao.isBlank() || valorStr == null || valorStr.isBlank() || data == null || tipo == null) {
                new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
                return;
            }
            if (usuarioLogado == null) {
                new Alert(AlertType.ERROR, "Erro crítico: Nenhum usuário logado.").showAndWait();
                return;
            }

            BigDecimal valor = new BigDecimal(valorStr);
            boolean gastoFixo = tipo.equalsIgnoreCase("Fixo");

            Gasto novoGasto = new Gasto();
            novoGasto.setNomeGasto(descricao);
            novoGasto.setValorGasto(valor);
            novoGasto.setData(data);
            novoGasto.setGastoFixo(gastoFixo);
            novoGasto.setUsuarioId(usuarioLogado.getId());
            
            System.out.println("Salvando gasto: " + descricao + " para usuário: " + usuarioLogado.getId());
            
            // Salvar no banco
            serviceGasto.create(novoGasto);
            
            // Limpar o cache para garantir que a próxima consulta traga dados atualizados
            serviceGasto.clearCache();
            
            // Publicar evento de novo gasto cadastrado
            EventBus.getInstance().publish(new EventBus.Event("GASTO_CADASTRADO", novoGasto));

            new Alert(AlertType.INFORMATION, "Gasto '" + descricao + "' cadastrado com sucesso!").showAndWait();
            
            // Fechar a janela se for um pop-up
            Stage stage = (Stage) cadGastoDescricao.getScene().getWindow();
            if (stage.getOwner() != null) {
                stage.close();
            }

        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Erro ao salvar gasto: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void limparTela(ActionEvent event) {
        cadGastoDescricao.setText("");
        cadGastoValor.setText("");
        cadGastoData.setValue(null);
        choiceBoxTipo.setValue(null);
    }
}