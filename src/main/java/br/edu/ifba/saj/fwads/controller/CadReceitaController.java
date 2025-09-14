package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.model.Receita;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.Service;
import br.edu.ifba.saj.fwads.bus.EventBus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CadReceitaController {

    @FXML
    private DatePicker cadReceitaData;
    @FXML
    private TextField cadReceitaDescricao;
    @FXML
    private TextField cadReceitaValor;

    private Service<Receita> serviceReceita = new Service<>(Receita.class);
    private Usuario usuarioLogado;

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        System.out.println("Usuário logado definido no CadReceitaController: " + usuarioLogado);
    }

    @FXML
    void salvarReceita(ActionEvent event) {
        try {
            String descricao = cadReceitaDescricao.getText();
            String valorStr = cadReceitaValor.getText();
            LocalDate data = cadReceitaData.getValue();

            if (descricao == null || descricao.isBlank() || valorStr == null || valorStr.isBlank() || data == null) {
                new Alert(AlertType.WARNING, "Preencha todos os campos antes de salvar.").showAndWait();
                return;
            }
            if (usuarioLogado == null) {
                new Alert(AlertType.ERROR, "Erro crítico: Nenhum usuário logado.").showAndWait();
                return;
            }

            BigDecimal valor = new BigDecimal(valorStr);

            Receita novaReceita = new Receita();
            novaReceita.setNome(descricao);
            novaReceita.setValor(valor);
            novaReceita.setData(data);
            novaReceita.setUsuarioId(usuarioLogado.getId());
            
            System.out.println("Salvando receita: " + descricao + " para usuário: " + usuarioLogado.getId());

            // Salvar no banco
            serviceReceita.create(novaReceita);
            
            // Limpar o cache para garantir que a próxima consulta traga dados atualizados
            serviceReceita.clearCache();
            
            // Publicar evento de nova receita cadastrada
            EventBus.getInstance().publish(new EventBus.Event("RECEITA_CADASTRADA", novaReceita));

            new Alert(AlertType.INFORMATION, "Receita '" + descricao + "' cadastrada com sucesso!").showAndWait();

            // Fechar a janela se for um pop-up
            Stage stage = (Stage) cadReceitaDescricao.getScene().getWindow();
            if (stage.getOwner() != null) {
                stage.close();
            }

        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Valor inválido! Digite um número válido.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Erro ao salvar receita: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void limparTela(ActionEvent event) {
        cadReceitaDescricao.setText("");
        cadReceitaValor.setText("");
        cadReceitaData.setValue(null);
    }
}