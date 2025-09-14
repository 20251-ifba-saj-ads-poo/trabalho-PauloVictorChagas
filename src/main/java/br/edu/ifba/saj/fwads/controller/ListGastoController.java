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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class ListGastoController implements EventBus.EventListener {

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
    private Usuario usuarioLogado;
    private ObservableList<Gasto> gastosObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        columnDataGasto.setCellValueFactory(new PropertyValueFactory<>("data"));
        columnDescricaoGasto.setCellValueFactory(new PropertyValueFactory<>("nomeGasto"));
        columnTipoGasto.setCellValueFactory(new PropertyValueFactory<>("tipoFormatado"));
        columnValorGasto.setCellValueFactory(new PropertyValueFactory<>("valorGasto"));
        
        // Configurar a TableView para usar a ObservableList
        tblGasto.setItems(gastosObservableList);
        
        // Registrar este controlador como listener de eventos
        EventBus.getInstance().subscribe(this);
        
        System.out.println("ListGastoController inicializado");
    }

    @Override
    public void onEvent(EventBus.Event event) {
        if ("GASTO_CADASTRADO".equals(event.getType())) {
            System.out.println("Evento GASTO_CADASTRADO recebido");
            Gasto novoGasto = (Gasto) event.getData();
            
            // Verificar se o gasto pertence ao usuário logado
            if (usuarioLogado != null && novoGasto.getUsuarioId().equals(usuarioLogado.getId())) {
                Platform.runLater(() -> {
                    // Adicionar à lista e forçar atualização da tabela
                    gastosObservableList.add(novoGasto);
                    System.out.println("Gasto adicionado à lista: " + novoGasto.getNomeGasto());
                    System.out.println("Total de gastos na lista: " + gastosObservableList.size());
                    
                    // Forçar refresh da tabela
                    tblGasto.refresh();
                });
            } else {
                System.out.println("Gasto não pertence ao usuário logado ou usuário é nulo");
            }
        }
    }

    public void loadGastoList(Usuario usuario) {
        System.out.println("Carregando lista de gastos para usuário: " + usuario);
        this.usuarioLogado = usuario;
        refreshList();
    }
    
    public void refreshList() {
        if (this.usuarioLogado != null) {
            System.out.println("Atualizando lista para usuário ID: " + usuarioLogado.getId());
            
            try {
                // Forçar limpeza do cache
                serviceGasto.clearCache();
                
                // Usar findAll e filtrar manualmente para evitar problemas de cache
                List<Gasto> todosGastos = serviceGasto.findAll();
                List<Gasto> gastosDoUsuario = todosGastos.stream()
                        .filter(g -> g.getUsuarioId().equals(usuarioLogado.getId()))
                        .toList();
                
                System.out.println("Gastos encontrados no banco (após filtro): " + gastosDoUsuario.size());
                
                Platform.runLater(() -> {
                    gastosObservableList.clear();
                    gastosObservableList.addAll(gastosDoUsuario);
                    
                    System.out.println(">>> DIAGNÓSTICO: Itens na tabela de GASTOS: " + gastosObservableList.size());
                    if (gastosObservableList.size() > 0) {
                        for (Gasto gasto : gastosObservableList) {
                            System.out.println(">>> Gasto na lista: " + gasto.getNomeGasto() + " - UserID: " + gasto.getUsuarioId());
                        }
                    }
                    
                    // Forçar refresh da tabela
                    tblGasto.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao carregar gastos: " + e.getMessage());
            }
        } else {
            System.out.println("Usuário logado é nulo! Não é possível carregar gastos.");
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
            
            // Forçar atualização após fechar a janela de cadastro
            refreshList();

        } catch(Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Não foi possível abrir a tela de cadastro.").show();
        }
    }
}