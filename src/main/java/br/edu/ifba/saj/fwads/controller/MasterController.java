package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.App;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.bus.EventBus;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MasterController implements EventBus.EventListener {

    @FXML
    private BorderPane masterPane;
    @FXML
    private VBox menu;
    @FXML
    private Button menuItemCadGasto;
    @FXML
    private Button menuItemCadReceita;
    @FXML
    private Button menuItemHome;
    @FXML
    private Button menuItemListGasto;
    @FXML
    private Button menuItemListReceita;
    @FXML
    private Label userEmail;
    @FXML
    private Circle userPicture;

    private Usuario usuarioLogado;
    private Map<String, Pane> viewCache = new HashMap<>();

    @FXML
    public void initialize() {
        // Registrar este controlador como listener de eventos
        EventBus.getInstance().subscribe(this);
    }

    @Override
    public void onEvent(EventBus.Event event) {
        // Limpar cache quando houver mudanças nos dados
        if ("GASTO_CADASTRADO".equals(event.getType()) || "RECEITA_CADASTRADA".equals(event.getType())) {
            viewCache.clear();
        }
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        setEmail(usuarioLogado.getEmail());
    }

    @FXML
    void logOff(MouseEvent event) {
        new Alert(AlertType.CONFIRMATION, "Deseja realmente sair?", ButtonType.YES, ButtonType.NO)
            .showAndWait()
            .filter(response -> response == ButtonType.YES)
            .ifPresent(response -> {
                viewCache.clear();
                EventBus.getInstance().unsubscribe(this);
                App.setRoot("controller/Login.fxml");
            });
    }

    @FXML
    void showHome(ActionEvent event) {
        limparBotoes(event.getSource());
        masterPane.setCenter(new Pane());
    }

    @FXML
    void showCadGasto(ActionEvent event) {
        limparBotoes(event.getSource());
        Pane view = getView("/br/edu/ifba/saj/fwads/controller/CadGasto.fxml");
        if (view != null) {
            CadGastoController controller = (CadGastoController) view.getProperties().get("controller");
            controller.setUsuarioLogado(this.usuarioLogado);
            masterPane.setCenter(view);
        }
    }

    @FXML
    void showListGasto(ActionEvent event) {
        limparBotoes(event.getSource());
        String fxmlPath = "/br/edu/ifba/saj/fwads/controller/ListGasto.fxml";
        Pane view = getView(fxmlPath);

        if (view != null) {
            ListGastoController controller = (ListGastoController) view.getProperties().get("controller");
            controller.loadGastoList(this.usuarioLogado);
            masterPane.setCenter(view);
        }
    }

    @FXML
    void showCadReceita(ActionEvent event) {
        limparBotoes(event.getSource());
        Pane view = getView("/br/edu/ifba/saj/fwads/controller/CadReceita.fxml");
        if (view != null) {
            CadReceitaController controller = (CadReceitaController) view.getProperties().get("controller");
            controller.setUsuarioLogado(this.usuarioLogado);
            masterPane.setCenter(view);
        }
    }

    @FXML
    void showListReceita(ActionEvent event) {
        limparBotoes(event.getSource());
        String fxmlPath = "/br/edu/ifba/saj/fwads/controller/ListReceita.fxml";
        Pane view = getView(fxmlPath);

        if (view != null) {
            ListReceitaController controller = (ListReceitaController) view.getProperties().get("controller");
            controller.loadReceitaList(this.usuarioLogado);
            masterPane.setCenter(view);
        }
    }
    
    private Pane getView(String fxmlPath) {
        if (viewCache.containsKey(fxmlPath)) {
            return viewCache.get(fxmlPath);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane view = loader.load();
            view.getProperties().put("controller", loader.getController());
            viewCache.put(fxmlPath, view);
            return view;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void limparBotoes(Object source) {
        menu.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
            }
        });
        if (source instanceof Button btn) {
            btn.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        }
    }

    private void setEmail(String email) {
        userEmail.setText(email);
    }
}