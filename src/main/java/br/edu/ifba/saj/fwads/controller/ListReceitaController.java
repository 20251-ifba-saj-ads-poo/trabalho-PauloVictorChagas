package br.edu.ifba.saj.fwads.controller;

import br.edu.ifba.saj.fwads.App;
import br.edu.ifba.saj.fwads.model.Receita;
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

public class ListReceitaController implements EventBus.EventListener {

    @FXML private TableColumn<Receita, LocalDate> columnDataReceita;
    @FXML private TableColumn<Receita, String> columnCategoriaReceita;
    @FXML private TableColumn<Receita, BigDecimal> columnValorReceita;
    @FXML private TableView<Receita> tblReceita;

    private Service<Receita> serviceReceita = new Service<>(Receita.class);
    private Usuario usuarioLogado;
    private ObservableList<Receita> receitasObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        columnDataReceita.setCellValueFactory(new PropertyValueFactory<>("data"));
        columnCategoriaReceita.setCellValueFactory(new PropertyValueFactory<>("nome"));
        columnValorReceita.setCellValueFactory(new PropertyValueFactory<>("valor"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        columnDataReceita.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : dtf.format(item));
            }
        });

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        columnValorReceita.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nf.format(item));
            }
        });

        tblReceita.setItems(receitasObservableList);
        EventBus.getInstance().subscribe(this);
    }

    @Override
    public void onEvent(EventBus.Event event) {
        if ("RECEITA_CADASTRADA".equals(event.getType())) {
            Platform.runLater(() -> {
                refreshList();
            });
        }
    }

    public void loadReceitaList(Usuario usuario) {
        this.usuarioLogado = usuario;
        refreshList();
    }

    public void refreshList() {
        if (this.usuarioLogado != null) {
            serviceReceita.clearCache();
            List<Receita> todasReceitas = serviceReceita.findAll();
            List<Receita> receitasDoUsuario = todasReceitas.stream()
                    .filter(r -> r.getUsuarioId().equals(usuarioLogado.getId()))
                    .collect(Collectors.toList());
            
            Platform.runLater(() -> {
                receitasObservableList.setAll(receitasDoUsuario);
                tblReceita.refresh();
            });
        }
    }

    @FXML
    void showNovoReceita(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("controller/CadReceita.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Nova Receita");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) tblReceita.getScene().getWindow());

            CadReceitaController controller = loader.getController();
            controller.setUsuarioLogado(this.usuarioLogado);

            stage.showAndWait();
            refreshList();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Não foi possível abrir a tela de cadastro.").show();
        }
    }
}