package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.App;
import br.edu.ifba.saj.fwads.model.Receita;
import br.edu.ifba.saj.fwads.service.Service;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class ListReceitaController {

    @FXML
    private TableColumn<Receita, String> columnDataReceita;

    @FXML
    private TableColumn<Receita, String> columnDescricaoReceita;

    @FXML
    private TableColumn<Receita, Double> columnValorReceita;

    @FXML
    private TableView<Receita> tblReceita;

    private Service<Receita> serviceReceita = new Service<>(Receita.class);

    @FXML
    public void initialize() {
        columnDataReceita.setCellValueFactory(new PropertyValueFactory<>("data"));
        columnDescricaoReceita.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        columnValorReceita.setCellValueFactory(new PropertyValueFactory<>("valor"));

        loadReceitaList();
    }

    public void loadReceitaList() {
        tblReceita.setItems(FXCollections.observableList(serviceReceita.findAll()));
    }

    @FXML
    void showNovoReceita(ActionEvent event) {
        Stage stage = new Stage();
        Scene scene = new Scene(App.loadFXML("controller/CadReceita.fxml"), 800, 600);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        CadReceitaController controller = (CadReceitaController) App.getController();
        controller.setListReceitaController(this);

        stage.showAndWait();
    }
}
