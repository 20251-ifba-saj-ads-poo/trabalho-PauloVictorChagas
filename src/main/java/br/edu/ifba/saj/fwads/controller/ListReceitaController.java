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
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class ListReceitaController implements EventBus.EventListener {

    @FXML
    private TableColumn<Receita, String> columnDataReceita;
    @FXML
    private TableColumn<Receita, String> columnDescricaoReceita;
    @FXML
    private TableColumn<Receita, Double> columnValorReceita;
    @FXML
    private TableView<Receita> tblReceita;

    private Service<Receita> serviceReceita = new Service<>(Receita.class);
    private Usuario usuarioLogado;
    private ObservableList<Receita> receitasObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        columnDataReceita.setCellValueFactory(new PropertyValueFactory<>("data"));
        columnDescricaoReceita.setCellValueFactory(new PropertyValueFactory<>("nome"));
        columnValorReceita.setCellValueFactory(new PropertyValueFactory<>("valor"));
        
        // Configurar a TableView para usar a ObservableList
        tblReceita.setItems(receitasObservableList);
        
        // Registrar este controlador como listener de eventos
        EventBus.getInstance().subscribe(this);
        
        System.out.println("ListReceitaController inicializado");
    }

    @Override
    public void onEvent(EventBus.Event event) {
        if ("RECEITA_CADASTRADA".equals(event.getType())) {
            System.out.println("Evento RECEITA_CADASTRADA recebido");
            Receita novaReceita = (Receita) event.getData();
            
            // Verificar se a receita pertence ao usuário logado
            if (usuarioLogado != null && novaReceita.getUsuarioId().equals(usuarioLogado.getId())) {
                Platform.runLater(() -> {
                    // Adicionar à lista e forçar atualização da tabela
                    receitasObservableList.add(novaReceita);
                    System.out.println("Receita adicionada à lista: " + novaReceita.getNome());
                    System.out.println("Total de receitas na lista: " + receitasObservableList.size());
                    
                    // Forçar refresh da tabela
                    tblReceita.refresh();
                });
            } else {
                System.out.println("Receita não pertence ao usuário logado ou usuário é nulo");
            }
        }
    }

    public void loadReceitaList(Usuario usuario) {
        System.out.println("Carregando lista de receitas para usuário: " + usuario);
        this.usuarioLogado = usuario;
        refreshList();
    }
    
    public void refreshList() {
        if (this.usuarioLogado != null) {
            System.out.println("Atualizando lista para usuário ID: " + usuarioLogado.getId());
            
            try {
                // Forçar limpeza do cache
                serviceReceita.clearCache();
                
                // Usar findAll e filtrar manualmente para evitar problemas de cache
                List<Receita> todasReceitas = serviceReceita.findAll();
                List<Receita> receitasDoUsuario = todasReceitas.stream()
                        .filter(r -> r.getUsuarioId().equals(usuarioLogado.getId()))
                        .toList();
                
                System.out.println("Receitas encontradas no banco (após filtro): " + receitasDoUsuario.size());
                
                Platform.runLater(() -> {
                    receitasObservableList.clear();
                    receitasObservableList.addAll(receitasDoUsuario);
                    
                    System.out.println(">>> DIAGNÓSTICO: Itens na tabela de RECEITAS: " + receitasObservableList.size());
                    if (receitasObservableList.size() > 0) {
                        for (Receita receita : receitasObservableList) {
                            System.out.println(">>> Receita na lista: " + receita.getNome() + " - UserID: " + receita.getUsuarioId());
                        }
                    }
                    
                    // Forçar refresh da tabela
                    tblReceita.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao carregar receitas: " + e.getMessage());
            }
        } else {
            System.out.println("Usuário logado é nulo! Não é possível carregar receitas.");
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
            
            // Forçar atualização após fechar a janela de cadastro
            refreshList();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Não foi possível abrir a tela de cadastro.").show();
        }
    }
}