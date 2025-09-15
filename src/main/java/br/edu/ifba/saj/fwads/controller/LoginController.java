package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.App;
import br.edu.ifba.saj.fwads.exception.LoginInvalidoException;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.UsuarioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private PasswordField txSenha;
    @FXML private TextField txUsuario;

    private UsuarioService usuarioService = new UsuarioService();

    @FXML
    void entrar(ActionEvent event) {
        try {
            Usuario usuario = usuarioService.validaLogin(txUsuario.getText(), txSenha.getText());
            App.setRoot("controller/Master.fxml");
            var controller = (br.edu.ifba.saj.fwads.controller.MasterController) App.getController();
            controller.setUsuarioLogado(usuario);
        } catch (LoginInvalidoException e) {
            new Alert(AlertType.ERROR, e.getMessage()).showAndWait();
        } catch (Exception e){
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Erro inesperado, favor entre em contato com a equipe de desenvolvimento").showAndWait();
        }
    }

    @FXML
    void limparCampos(ActionEvent event) {
        txUsuario.setText("");
        txSenha.setText("");
    }

    @FXML
    private void initialize() {
    }
}
