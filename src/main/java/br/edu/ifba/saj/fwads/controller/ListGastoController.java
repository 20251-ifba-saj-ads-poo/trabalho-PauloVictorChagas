package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.App;
import br.edu.ifba.saj.fwads.model.Gasto;
import br.edu.ifba.saj.fwads.model.Usuario;
import br.edu.ifba.saj.fwads.service.Service;
import br.edu.ifba.saj.fwads.bus.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ListGastoController implements EventBus.EventListener {

    @FXML private TableColumn<Gasto, LocalDate> columnDataGasto;
    @FXML private TableColumn<Gasto, String> columnCategoriaGasto;
    @FXML private TableColumn<Gasto, String> columnTipoGasto;
    @FXML private TableColumn<Gasto, BigDecimal> columnValorGasto;
    @FXML private TableView<Gasto> tblGasto;

    private Service<Gasto> serviceGasto = new Service<>(Gasto.class);
    private Usuario usuarioLogado;
    private ObservableList<Gasto> gastosObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        columnDataGasto.setCellValueFactory(new PropertyValueFactory<>("data"));
        columnCategoriaGasto.setCellValueFactory(new PropertyValueFactory<>("nomeGasto"));
        columnTipoGasto.setCellValueFactory(new PropertyValueFactory<>("tipoFormatado"));
        columnValorGasto.setCellValueFactory(new PropertyValueFactory<>("valorGasto"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        columnDataGasto.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : dtf.format(item));
            }
        });

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        columnValorGasto.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nf.format(item));
            }
        });

        tblGasto.setItems(gastosObservableList);
        EventBus.getInstance().subscribe(this);
    }

    @Override
    public void onEvent(EventBus.Event event) {
        if ("GASTO_CADASTRADO".equals(event.getType())) {
            Platform.runLater(() -> {
                refreshList();
            });
        }
    }

    public void loadGastoList(Usuario usuario) {
        this.usuarioLogado = usuario;
        refreshList();
    }
    

   public void refreshList() {
    if (this.usuarioLogado != null) {
        System.out.println("DEBUG: usuário atual id = " + usuarioLogado.getId());
        serviceGasto.clearCache();
        List<Gasto> todosGastos = serviceGasto.findAll();
        List<Gasto> gastosDoUsuario = todosGastos.stream()
                .filter(g -> g.getUsuarioId().equals(usuarioLogado.getId()))
                .collect(Collectors.toList());
        
        System.out.println("DEBUG: gastosDoUsuario.size() = " + gastosDoUsuario.size());
        
        Platform.runLater(() -> {
            gastosObservableList.setAll(gastosDoUsuario);
            tblGasto.refresh();
        });
    }
}

    @FXML
    void showNovoGasto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("controller/CadGasto.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Novo Gasto");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) tblGasto.getScene().getWindow());

            CadGastoController controller = loader.getController();
            controller.setUsuarioLogado(this.usuarioLogado);

            stage.showAndWait();
            refreshList();

        } catch(Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Não foi possível abrir a tela de cadastro.").show();
        }
    }
}