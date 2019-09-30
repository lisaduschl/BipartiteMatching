/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bipartitematching;

import View.MainView;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class BipartiteMatching extends Application {
    
    public static Stage MainStage;
    public MainView main_view;
    
    @Override
    public void start(Stage primaryStage) {
        Resource.initWindowSize();
        
        main_view = new MainView();
        main_view.initView();
        
        MainStage = primaryStage;
        
        Scene scene = new Scene(main_view.main_pane);
        scene.getStylesheets().add(getClass().getResource("Styles.css").toExternalForm());
        MainStage.setTitle("Bipartite Matching");
        MainStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("View/icon.png")));
        MainStage.setScene(scene);
        MainStage.setMaximized(true);
        MainStage.setResizable(true);
        MainStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
