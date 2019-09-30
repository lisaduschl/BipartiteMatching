/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Edge;
import Controller.Graph;
import bipartitematching.BipartiteMatching;
import bipartitematching.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class ResultView {

    private Stage result_stage;
    private BorderPane result_pane = new BorderPane();

    public ListView<String> result_list = new ListView<String>();

    public Label cross_label = new Label();
    public Label smallest_label = new Label();
    public Label message_label = new Label();
    public Label reorder1_label = new Label();
    public Label reorder2_label = new Label();

    private Button close_btn;

    public Graph graph;
    
    public Integer[] idx1;
    public Integer[] idx2;

    public void initView() {
        cross_label.setId("roundLabel");
        reorder1_label.setId("roundLabel");
        reorder2_label.setId("roundLabel");
        smallest_label.setId("roundLabel");
        message_label.setId("roundLabel");
        
        result_list.prefWidthProperty().bind(result_pane.widthProperty().subtract(80));
        initResults_BaryCenter();        

        VBox main_panel = new VBox();
        main_panel.setSpacing(5);
        main_panel.getChildren().addAll(smallest_label, message_label, cross_label, reorder1_label, reorder2_label, result_list);
        
        GroupBox mainBox = new GroupBox("Result", main_panel, Resource.WINDOW_WIDTH - 60, 15);

        initBtns();
        HBox btnBox = new HBox();
        btnBox.setSpacing(15);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(close_btn);
        GroupBox btnGroup = new GroupBox("Select Required Option", btnBox, Resource.WINDOW_WIDTH - 60, 20);

        BorderPane blue_pane = new BorderPane();
        blue_pane.setId("main_area");
        BorderPane.setMargin(mainBox, new Insets(20, 10, 10, 10));
        BorderPane.setAlignment(mainBox, Pos.CENTER);
        blue_pane.setCenter(mainBox);
        BorderPane.setAlignment(btnGroup, Pos.CENTER);
        BorderPane.setMargin(btnGroup, new Insets(10, 10, 25, 10));
        blue_pane.setBottom(btnGroup);

        result_pane.setId("black_area");
        BorderPane.setMargin(blue_pane, new Insets(20, 20, 20, 20));
        result_pane.setCenter(blue_pane);

        Scene scene = new Scene(result_pane);
        scene.getStylesheets().add(getClass().getResource("Styles.css").toExternalForm());

        result_stage = new Stage();
        result_stage.setScene(scene);
        result_stage.setTitle("Result");
        InputStream icon_stream = getClass().getClassLoader().getResourceAsStream("View/icon.png");
        Image icon = new Image(icon_stream);
        result_stage.getIcons().add(icon);

        result_stage.initOwner(BipartiteMatching.MainStage);
        result_stage.initModality(Modality.WINDOW_MODAL);
    }

    public void show() {
        result_stage.show();
    }

    public void initResults() {
        int cross_num = 0;
        HashMap<Integer, Integer> edge_list = new HashMap<Integer, Integer>();
        edge_list.put(0, graph.vertex_num_1 - 1);

        boolean full_check = true;

        for (int i = 0; i < graph.vertex_num_1; i++) {
            String result_str = new String();
            Edge edge = graph.sets.get(0).get(i);
            if (edge.getGap() == -2) {
                full_check = false;
                continue;
            }
            if (edge.getGap() <= 0) {
                continue;
            }
            int gaps = edge.getGap();
            result_str = "From Bottom " + edge.getFrom() + "  To Top " + edge.getTo() + "   Gaps: " + gaps;
            cross_num += gaps;
            result_list.getItems().add(result_str);
        }
        for (int k = 1; k < graph.vertex_num_2; k++) {
            for (int i = 0; i < graph.vertex_num_1; i++) {
                String result_str = new String();
                Edge edge = graph.sets.get(k).get(i);
                if (edge.getGap() == -2) {
                    full_check = false;
                    continue;
                }
                if (edge.getGap() <= 0) {
                    continue;
                }
                int gaps = edge.getGap();
                result_str = "From Bottom " + edge.getFrom() + "  To Top " + edge.getTo() + "   Gaps: " + gaps;
                cross_num += gaps;
                result_list.getItems().add(result_str);
            }
        }
        cross_label.setText("Crossings: " + String.valueOf(cross_num));

        //get smallest gaps number
        int smallest = graph.getSmallestGaps();
        smallest_label.setText("Smallest k for full connection: " + smallest);

        //check full option
        if (!full_check) {
            message_label.setText("Greater k needed for full connections!");
        } else {
            message_label.setText("Gap number (k) allows full connections!");
        }

    }
    
    public void initResults_BaryCenter() {
        int cross_num = 0;
        HashMap<Integer, Integer> edge_list = new HashMap<Integer, Integer>();
        edge_list.put(0, graph.vertex_num_1 - 1);

        boolean full_check = true;

        for (int i = 0; i < graph.vertex_num_1; i++) {
            String result_str = new String();
            Edge edge = graph.sets.get(0).get(i);
            if (edge.getGap() == -2) {
                full_check = false;
                continue;
            }
            if (edge.getGap() <= 0) {
                continue;
            }
            int gaps = edge.getGap();
            result_str = "From Bottom " + edge.getFrom() + "  To Top " + edge.getTo() + "   Gaps: " + gaps;
            cross_num += gaps;
            result_list.getItems().add(result_str);
        }
        for (int k = 1; k < graph.vertex_num_2; k++) {
            for (int i = 0; i < graph.vertex_num_1; i++) {
                String result_str = new String();
                Edge edge = graph.sets.get(k).get(i);
                if (edge.getGap() == -2) {
                    full_check = false;
                    continue;
                }
                if (edge.getGap() <= 0) {
                    continue;
                }
                int gaps = edge.getGap();
                result_str = "From Bottom " + edge.getFrom() + "  To Top " + edge.getTo() + "   Gaps: " + gaps;
                cross_num += gaps;
                result_list.getItems().add(result_str);
            }
        }
        cross_label.setText("Crossings: " + String.valueOf(cross_num));

        //get smallest gaps number
        int smallest = graph.getSmallestGaps();
        smallest_label.setText("Smallest k for full connection: " + smallest);

        if (!full_check) {
            message_label.setText("Greater k needed for full connections!");
        } else {
            message_label.setText("Gap number (k) allows full connections!");
        }
        
        
        String result_str = "Reordering 1st layer: ";
        for (int i = 0; i < graph.vertex_num_1; i++)
            result_str += (idx1[i] + 1) + " ";
        reorder1_label.setText(result_str);
        
        result_str = "Reordering 2nd layer: ";
        for (int i = 0; i < graph.vertex_num_2; i++)
            result_str += (idx2[i] + 1) + " ";
         reorder2_label.setText(result_str);
    }

    //Here index array(of length equal to length of d array) contains the numbers from 0 to length of d array   
    public static Integer [] SortWithIndex(double[] data, Integer[] dims, Integer[] index)
    {
        int len = data.length;
        double temp1[] = new double[len];
        int temp2[] = new int[len];

         for (int i = 0; i <len; i++) {
            for (int j = i + 1; j < len; j++) {
                    if (data[i] > data[j]) {
                          temp1[i] = data[i];
                          data[i] = data[j];
                          data[j] = temp1[i];

                          temp2[i] = index[i];
                          index[i] = index[j];
                          index[j] = temp2[i];
                     }
                    if (data[i] == data[j]) {
                        if (dims[i] < dims[j]) {
                            temp1[i] = data[i];
                            data[i] = data[j];
                            data[j] = temp1[i];

                            temp2[i] = index[i];
                            index[i] = index[j];
                            index[j] = temp2[i];
                        }
                    }
              }
        }
        return index;
    }
      
    private void initBtns() {
        close_btn = new Button("Close");
        close_btn.setId("mainbtn");
        close_btn.setTextAlignment(TextAlignment.CENTER);
        close_btn.setPrefWidth(180);

        close_btn.setOnAction((evt) -> {
            result_stage.close();
        });
        close_btn.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                result_stage.close();
            }
        });

    }
}
