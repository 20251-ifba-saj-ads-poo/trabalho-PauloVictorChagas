package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.App;
import br.edu.ifba.saj.fwads.model.Gasto;
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

public class ListGastoController {

    @FXML
    private TableColumn<Gasto, String> columnDataGasto;

    @FXML
    private TableColumn<Gasto, String> columnDescricaoGasto;

    @FXML
    private TableColumn<Gasto, String> columnTipoGasto;

    @FXML
    private TableColumn<Gasto, Double> columnValorGasto;

    @FXML
    private TableView<Gasto> tblGasto;

    private Service<Gasto> serviceGasto = new Service<>(Gasto.class);

    @FXML
    public void initialize() {
        columnDataGasto.setCellValueFactory(new PropertyValueFactory<>("data"));
        columnDescricaoGasto.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        columnTipoGasto.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        columnValorGasto.setCellValueFactory(new PropertyValueFactory<>("valor"));

        loadGastoList();
    }

    public void loadGastoList() {
        tblGasto.setItems(FXCollections.observableList(serviceGasto.findAll()));
    }

    @FXML
    void showNovoGasto(ActionEvent event) {
        Stage stage = new Stage();
        Scene scene = new Scene(App.loadFXML("controller/CadGasto.fxml"), 800, 600);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        CadGastoController controller = (CadGastoController) App.getController();
        controller.setListGastoController(this);

        stage.showAndWait();
    }
}
