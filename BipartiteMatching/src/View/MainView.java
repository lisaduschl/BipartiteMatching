/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Edge;
import Controller.GapPoint;
import Controller.Graph;
import Controller.Line;
import Controller.Point;
import Controller.PointCompare;
import bipartitematching.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;


public class MainView {

    public BorderPane gray_pane = new BorderPane();
    public BorderPane main_pane = new BorderPane();

    Canvas graph_view;
    GraphicsContext gc;
    BorderPane canvas_pane = new BorderPane();

    public Label vertex_label_1 = new Label("m: ");
    public Label vertex_label_2 = new Label("n: ");
    public TextField vertex_1 = new TextField();
    public TextField vertex_2 = new TextField();

    public Label gap_label = new Label("Gaps k: ");
    public TextField gap = new TextField();

    Button view_graph_btn = new Button("View Graph");
    Button analysis_btn = new Button("Result");
    public Button close_btn;
    
    List<Line> deleted_lines = new ArrayList<Line>();

    HashMap<String, String> source_list = new HashMap<String, String>();

    private Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

    public List<Point> dim_1 = new ArrayList<Point>();
    public List<Point> dim_2 = new ArrayList<Point>();

    public double y_bound_1 = 0;
    public double y_bound_2 = 0;
    public double radius = 20;

    Graph graph;
    Integer[] idx1;
    Integer[] idx2;
    int[][] allow_lines;
    int[][] new_allow_lines;

    /**
     * init main windows
     */
    public void initView() {
        initButtonGroup();

        graph_view = new Canvas(Resource.WINDOW_WIDTH - 120, Resource.WINDOW_HEIGHT - 300);
        gc = graph_view.getGraphicsContext2D();
        canvas_pane.setId("border");
        canvas_pane.setCenter(graph_view);

        GroupBox mainBox = new GroupBox("Bipartite Matching K(m,n)", canvas_pane, Resource.WINDOW_WIDTH, 25);
        gray_pane.setId("main_area");

        BorderPane.setMargin(mainBox, new Insets(30, 20, 20, 20));
        BorderPane.setAlignment(graph_view, Pos.TOP_CENTER);
        gray_pane.setCenter(mainBox);

        vertex_label_1.setId("roundLabel");
        vertex_label_2.setId("roundLabel");
        vertex_1.setId("round");
        vertex_2.setId("round");
        vertex_1.setPrefWidth(70);
        vertex_2.setPrefWidth(70);
        vertex_1.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) {
                if (!validEditingState.matcher(newValue).matches()) {
                    vertex_1.setText(oldValue);
                }
            }
        });
        vertex_2.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) {
                if (!validEditingState.matcher(newValue).matches()) {
                    vertex_1.setText(oldValue);
                }
            }
        });
        HBox vertexBox1 = new HBox();
        vertexBox1.getChildren().addAll(vertex_label_1, vertex_1);

        HBox vertexBox2 = new HBox();
        vertexBox2.getChildren().addAll(vertex_label_2, vertex_2);

        gap_label.setId("roundLabel");
        gap.setId("round");
        gap.setPrefWidth(70);
        gap.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) {
                if (!validEditingState.matcher(newValue).matches()) {
                    gap.setText(oldValue);
                }
            }
        });
        HBox gapBox = new HBox();
        gapBox.getChildren().addAll(gap_label, gap);

        HBox btnBox = new HBox();
        btnBox.setSpacing(20);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(view_graph_btn, analysis_btn);

        Region region = new Region();

        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(region, Priority.ALWAYS);
        bottomBox.setSpacing(25);
        bottomBox.getChildren().addAll(vertexBox1, vertexBox2, gapBox, btnBox);

        GroupBox button_Group = new GroupBox("Select Required Option", bottomBox, Resource.WINDOW_WIDTH, 30);

        BorderPane.setAlignment(button_Group, Pos.CENTER);
        BorderPane.setMargin(button_Group, new Insets(10, 20, 20, 20));
        gray_pane.setBottom(button_Group);

        main_pane.setId("black_area");
        main_pane.setPrefWidth(Resource.WINDOW_WIDTH);
        BorderPane.setMargin(gray_pane, new Insets(20, 20, 20, 20));
        main_pane.setCenter(gray_pane);

        canvasEventFiltering();
    }

    private void initButtonGroup() {
        view_graph_btn.setId("mainbtn");
        view_graph_btn.setPrefWidth(180);
        analysis_btn.setId("mainbtn");
        analysis_btn.setPrefWidth(180);

        // draw graph when click View Graph button
        view_graph_btn.setOnAction((evt) -> {
            if (!check()) {
                return;
            }
            int gap_number = Integer.valueOf(gap.getText());
            int vertex_number_1 = Integer.valueOf(vertex_1.getText());
            int vertex_number_2 = Integer.valueOf(vertex_2.getText());
            allow_lines = new int[vertex_number_2][vertex_number_1];
            view_graph_btn.setDisable(true);
            view_graph_btn.setText("Drawing...");
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        graph = new Graph();
                        graph.max_gap_num = gap_number;
                        graph.vertex_num_1 = vertex_number_1;
                        graph.vertex_num_2 = vertex_number_2;
                        graph.allow_lines = allow_lines;
                        graph.constructGraph();
                        graph.processingBPM(); // start algorithm
                        drawGraph(); // draw graph
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.setOnFailed(e -> {
                Throwable exception = e.getSource().getException();
                if (exception != null) {
                    view_graph_btn.setDisable(false);
                    view_graph_btn.setText("View Graph");
                }
            });
            task.setOnSucceeded(e -> {
                deleted_lines.clear();
                view_graph_btn.setDisable(false);
                view_graph_btn.setText("View Graph");
            });
            new Thread(task).start();

        });
        //get result when click Result button
        analysis_btn.setOnAction((evt) -> {
            if (!check()) {
                return;
            }
            int gap_number = Integer.valueOf(gap.getText());
            int vertex_number_1 = Integer.valueOf(vertex_1.getText());
            int vertex_number_2 = Integer.valueOf(vertex_2.getText());
            analysis_btn.setDisable(true);
            analysis_btn.setText("Processing...");
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        if (allow_lines == null) {
                            allow_lines = new int[vertex_number_2][vertex_number_1];

                        } else {
                            if (allow_lines.length != vertex_number_2 || allow_lines[0].length != vertex_number_1) {
                                allow_lines = new int[vertex_number_2][vertex_number_1];
                            }
                        }
                        graph = new Graph();
                        graph.max_gap_num = gap_number;

                        graph.vertex_num_1 = vertex_number_1;
                        graph.vertex_num_2 = vertex_number_2;
                        graph.allow_lines = allow_lines;
                        graph.constructGraph();
                        graph.processingBPM();
                        
                        for (int i = 0; i < deleted_lines.size(); i++) {
                            int ind = graph.includeEdge(deleted_lines.get(i));
                            graph.all_edges.remove(ind);
                        }
                        
                        /***Barycenter Heuristic Algorithm***/
                        // Get neighbourhood matrix
                        double[][] mat = new double[graph.vertex_num_1][graph.vertex_num_2];

                        for (int i = 0; i < graph.all_edges.size(); i++) {
                            mat[graph.all_edges.get(i).from][graph.all_edges.get(i).to] = 1;
                        }

                        // Reordering 1st layer
                        double[] bary_list = new double[graph.vertex_num_2];
                        Integer[] dim_list = new Integer[graph.vertex_num_2];
                        for (int i = 0; i < graph.vertex_num_2; i++) {
                            int dim = 0;
                            double sum = 0.0;
                            for (int j = 0; j < graph.vertex_num_1; j++) {
                                if (mat[j][i] > 0) {
                                    dim++;
                                    sum += (double)(j + 1);
                                }
                            }
                            if (dim == 0)
                                bary_list[i] = 0;
                            else
                                bary_list[i] = sum / (double)dim;
                            dim_list[i] = dim;
                        }

                        idx2 = new Integer[graph.vertex_num_2];
                        for (int i = 0; i < graph.vertex_num_2; i++)
                            idx2[i] = i;
                        idx2 = SortWithIndex(bary_list, dim_list, idx2);

                        // Reorder layer 2
                        double[][] mat1 = new double[graph.vertex_num_1][graph.vertex_num_2];
                        for (int i = 0; i < graph.vertex_num_1; i++) {
                            for (int j = 0; j < graph.vertex_num_2; j++) {
                                mat1[i][j] = mat[i][idx2[j]];
                            }
                        }

                        double[] bary_list1 = new double[graph.vertex_num_1];
                        Integer[] dim_list1 = new Integer[graph.vertex_num_1];
                        for (int i = 0; i < graph.vertex_num_1; i++) {
                            int dim = 0;
                            double sum = 0.0;
                            for (int j = 0; j < graph.vertex_num_2; j++) {
                                if (mat1[i][j] > 0) {
                                    dim++;
                                    sum += (double)(j + 1);
                                }
                            }
                            if (dim == 0)
                                bary_list1[i] = 0;
                            else
                                bary_list1[i] = sum / (double)dim;
                            dim_list1[i] = dim;
                        }

                        idx1 = new Integer[graph.vertex_num_1];
                        for (int i = 0; i < graph.vertex_num_1; i++)
                            idx1[i] = i;
                        idx1 = SortWithIndex(bary_list1, dim_list1, idx1);
                        
                        new_allow_lines = new int[vertex_number_2][vertex_number_1];
                        for (int i = 0; i < graph.vertex_num_2; i++) {
                            for (int j = 0; j < graph.vertex_num_1; j++) {
                                new_allow_lines[i][j] = allow_lines[idx2[i]][idx1[j]];
                            }
                        }
                        
                        allow_lines = new_allow_lines;
                        graph.allow_lines = new_allow_lines;
                        graph.constructGraph();
                        graph.processingBPM();
                        drawGraph();
                        drawGraphForDrag();
                        
                        Platform.runLater(() -> {
                            ResultView rst_view = new ResultView();
                            rst_view.graph = graph;
                            rst_view.idx1 = idx1;
                            rst_view.idx2 = idx2;
                            rst_view.initView();
                            rst_view.show();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.setOnFailed(e -> {
                Throwable exception = e.getSource().getException();
                if (exception != null) {
                    analysis_btn.setDisable(false);
                    analysis_btn.setText("Result");
                }
            });
            task.setOnSucceeded(e -> {
                analysis_btn.setDisable(false);
                analysis_btn.setText("Result");
            });
            new Thread(task).start();

        });
    }
    
    /**
     * Check entered values are correct or not
     * @return 
     */
    private boolean check() {
        if (vertex_1.getText().isEmpty()) {
            AlertMessage.message("Please enter top vertex number!");
            return false;
        }
        if (vertex_2.getText().isEmpty()) {
            AlertMessage.message("Please enter bottom vertex number!");
            return false;
        }
        if (gap.getText().isEmpty()) {
            AlertMessage.message("Please enter maximum gap number!");
            return false;
        }
        int gap_number = Integer.valueOf(gap.getText());
        int vertex_number_1 = Integer.valueOf(vertex_1.getText());
        int vertex_number_2 = Integer.valueOf(vertex_2.getText());
        if (gap_number <= 0) {
            AlertMessage.message("Gap number must be greater than zero!");
            return false;
        }
        if (vertex_number_1 < 2) {
            AlertMessage.message("Vertex number must be greater than 2!");
            return false;
        }
        if (vertex_number_2 < 2) {
            AlertMessage.message("Vertex number must be greater than 2!");
            return false;
        }
        return true;
    }
    /**
     * Draw graph
     */
    private void drawGraph() {
        if (graph == null) {
            return;
        }
        double width = graph_view.getWidth();
        double height = graph_view.getHeight();
        gc.clearRect(0, 0, width, height);
        gc.setLineWidth(1.0);
        gc.setFill(Color.BLACK);
        //draw two lines first
        y_bound_1 = height / 5;
        y_bound_2 = height / 5 * 4;

        gc.strokeLine(0, y_bound_1, width, y_bound_1);
        gc.strokeLine(0, y_bound_2, width, y_bound_2);

        gc.setFill(Color.RED);

        dim_1.clear();
        dim_2.clear();

        //draw top layer
        double interval = (double) width / (double) (graph.vertex_num_1 + 1);
        for (int i = 0; i < graph.vertex_num_1; i++) {
            gc.fillOval(interval * (i + 1) - (radius / 2), y_bound_1 - (radius / 2), radius, radius);
            dim_2.add(new Point(interval * (i + 1), y_bound_1));
        }
        //draw bottom layer
        interval = (double) width / (double) (graph.vertex_num_2 + 1);
        for (int i = 0; i < graph.vertex_num_2; i++) {
            gc.fillOval(interval * (i + 1) - (radius / 2), y_bound_2 - (radius / 2), radius, radius);
            dim_1.add(new Point(interval * (i + 1), y_bound_2));
        }

        //draw edge
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.BLUE);

        int[][] draw_option = new int[graph.vertex_num_2][graph.vertex_num_1];
        for (int k = 0; k < graph.vertex_num_2; k++) {
            for (int i = 0; i < graph.vertex_num_1; i++) {
                draw_option[k][i] = allow_lines[k][i];
            }
        }
        for (int k = 0; k < graph.vertex_num_2; k++) {
            for (int i = 0; i < graph.vertex_num_1; i++) {
                if (draw_option[k][i] != 0) {
                    continue;
                }
                Edge edge = graph.sets.get(k).get(i);
                if (edge.getGap() == 0) {
                    if (i == graph.vertex_num_1 - 1) {
                        gc.strokeLine(dim_1.get(k).x, dim_1.get(k).y, dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y);
                        draw_option[k][i] = 1;
                        continue;
                    }
                    continue;
                }
                if (edge.getGap() == -2) {
                    continue;
                }
                if (edge.getGap() == -1) {
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(dim_1.get(k).x, dim_1.get(k).y, dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y);
                    gc.setStroke(Color.BLUE);
                    draw_option[k][i] = 1;
                    continue;
                }

                List<GapPoint> cross_list = edge.cross_list;
                if (cross_list.isEmpty()) {
                    continue;
                }
                draw_option[k][i] = 1;

                //draw cross list line
                //if it is not drawn, draw it
                for (int gg = 0; gg < cross_list.size(); gg++) {
                    GapPoint pot = cross_list.get(gg);
                    if (draw_option[pot.x - 1][pot.y - 1] == 0) {
                        Edge check_edge = graph.sets.get(pot.x - 1).get(pot.y - 1);
                        if (check_edge.getGap() == 0) {
                            gc.strokeLine(dim_1.get(check_edge.getFrom() - 1).x, dim_1.get(check_edge.getFrom() - 1).y,
                                    dim_2.get(check_edge.getTo() - 1).x, dim_2.get(check_edge.getTo() - 1).y);
                            draw_option[pot.x - 1][pot.y - 1] = 1;
                        }
                    }
                }
                Point cur_pos_1 = dim_1.get(edge.getFrom() - 1);
                Point cur_pos_2 = dim_2.get(edge.getTo() - 1);

                double angle = getAngle(cur_pos_1, cur_pos_2);
                Point prev_point = null;
                List<Double> gap_list = new ArrayList<Double>();

                if (k == 2) {
                    int t = 0;
                }
                Point check_point = null;
                List<Integer> remove_index_list = new ArrayList<Integer>();
                for (int gg = 0; gg < cross_list.size(); gg++) {
                    GapPoint gap = cross_list.get(gg);
                    Point pos_1 = dim_1.get(gap.x - 1);
                    Point pos_2 = dim_2.get(gap.y - 1);
                    Point point = lineLineIntersection(cur_pos_1, cur_pos_2, pos_1, pos_2);
                    if (check_point != null) {
                        if (point.x == check_point.x && point.y == check_point.y) {
                            remove_index_list.add(gg); // remove same position gaps
                        }
                    }
                    cross_list.get(gg).cross_point = point;
                    check_point = point;
                }
                for (int index : remove_index_list) {
                    cross_list.remove(index);
                }
                //sort gaps
                Comparator<GapPoint> comparator = Comparator.comparingDouble(GapPoint::getCross);
                cross_list.sort(comparator);

                for (int gg = 0; gg < cross_list.size(); gg++) {
                    GapPoint gap = cross_list.get(gg);
                    Point point = gap.cross_point;
                    if (point.x == Double.MAX_VALUE
                            && point.y == Double.MAX_VALUE) {
                        continue;
                    }
                    if (prev_point == null) {
                        double x_offset = point.x - cur_pos_2.x;
                        if (x_offset == 0) {
                            double len = Math.abs(cur_pos_2.y - point.y);
                            gap_list.add(len);
                        } else {
                            double len = Math.abs((double) x_offset / (Math.cos(Math.toRadians(angle))));
                            gap_list.add(len);
                        }
                    } else {
                        double x_offset = point.x - prev_point.x;
                        if (x_offset == 0) {
                            double len = Math.abs(prev_point.y - point.y) - 5;
                            gap_list.add(len);
                        } else {
                            double len = Math.abs((double) x_offset / (Math.cos(Math.toRadians(angle)))) - 5;
                            gap_list.add(len);
                        }
                    }
                    prev_point = point;
                }
                double len = 0;
                double x_offset = prev_point.x - cur_pos_1.x;
                if (x_offset == 0) {
                    len = Math.abs(prev_point.y - cur_pos_1.y);
                } else {
                    len = Math.abs((double) x_offset / (Math.cos(Math.toRadians(angle))));
                }
                double[] dash_array = new double[gap_list.size() * 2 + 1];
                for (int g = 0; g < gap_list.size(); g++) {
                    if (gap_list.get(g) < 5) {
                        dash_array[2 * g] = gap_list.get(g) - 1;
                        dash_array[2 * g + 1] = 1;
                    } else {
                        dash_array[2 * g] = gap_list.get(g) - 5;
                        dash_array[2 * g + 1] = 10;
                    }
                }
                dash_array[gap_list.size() * 2] = len;

                gc.setLineDashes(dash_array);
                gc.strokeLine(dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y,
                        dim_1.get(k).x, dim_1.get(k).y);

                gc.setLineDashes(null);
            }
        }
    }
    /**
     * drag & move and remove move when mouse drag and click
     */
    private void drawGraphForDrag() {
        if (graph == null) {
            return;
        }
        graph.allow_lines = allow_lines;
        graph.constructGraph();
        graph.processingBPM();

        double width = graph_view.getWidth();
        double height = graph_view.getHeight();

        gc.clearRect(0, 0, width, height);

        gc.setLineWidth(1.0);
        gc.setFill(Color.BLACK);

        gc.strokeLine(0, y_bound_1, width, y_bound_1);
        gc.strokeLine(0, y_bound_2, width, y_bound_2);

        gc.setFill(Color.RED);

        //draw top layer
        for (int i = 0; i < graph.vertex_num_1; i++) {
            Point point = dim_2.get(i);
            gc.fillOval(point.x - (radius / 2), y_bound_1 - (radius / 2), radius, radius);
        }
        //draw bottom layer
        for (int i = 0; i < graph.vertex_num_2; i++) {
            Point point = dim_1.get(i);
            gc.fillOval(point.x - (radius / 2), y_bound_2 - (radius / 2), radius, radius);
        }

        //draw edge
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.BLUE);

        //int[][] draw_option = allow_lines;
        int[][] draw_option = new int[graph.vertex_num_2][graph.vertex_num_1];
        for (int k = 0; k < graph.vertex_num_2; k++) {
            for (int i = 0; i < graph.vertex_num_1; i++) {
                draw_option[k][i] = allow_lines[k][i];
            }
        }
        for (int k = 0; k < graph.vertex_num_2; k++) {
            for (int i = 0; i < graph.vertex_num_1; i++) {
                if (draw_option[k][i] != 0) {
                    continue;
                }
                Edge edge = graph.sets.get(k).get(i);
                if (edge.getGap() == 0) {
                    if (i == graph.vertex_num_1 - 1) {
                        gc.strokeLine(dim_1.get(k).x, dim_1.get(k).y, dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y);
                        draw_option[k][i] = 1;
                        continue;
                    }
                    ////////////////////
                    gc.strokeLine(dim_1.get(k).x, dim_1.get(k).y, dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y);
                    draw_option[k][i] = 1;
                    ////////////////////
                    continue;
                }
                if (edge.getGap() == -2) {
                    continue;
                }
                if (edge.getGap() == -1) {
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(dim_1.get(k).x, dim_1.get(k).y, dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y);
                    gc.setStroke(Color.BLUE);
                    draw_option[k][i] = 1;
                    continue;
                }

                List<GapPoint> cross_list = edge.cross_list;
                if (cross_list.isEmpty()) {
                    continue;
                }
                draw_option[k][i] = 1;

                //draw cross list line
                //if it is not drawn, draw it
                for (int gg = 0; gg < cross_list.size(); gg++) {
                    GapPoint pot = cross_list.get(gg);
                    if (draw_option[pot.x - 1][pot.y - 1] == 0) {
                        Edge check_edge = graph.sets.get(pot.x - 1).get(pot.y - 1);
                        if (check_edge.getGap() == 0) {
                            gc.strokeLine(dim_1.get(check_edge.getFrom() - 1).x, dim_1.get(check_edge.getFrom() - 1).y,
                                    dim_2.get(check_edge.getTo() - 1).x, dim_2.get(check_edge.getTo() - 1).y);
                            draw_option[pot.x - 1][pot.y - 1] = 1;
                        }
                    }
                }
                Point cur_pos_1 = dim_1.get(edge.getFrom() - 1);
                Point cur_pos_2 = dim_2.get(edge.getTo() - 1);

                double angle = getAngle(cur_pos_1, cur_pos_2);
                Point prev_point = null;
                List<Double> gap_list = new ArrayList<Double>();

                if (k == 2 && i == 1) {
                    int t = 0;
                }
                Point check_point = null;
                List<Integer> remove_index_list = new ArrayList<Integer>();
                for (int gg = 0; gg < cross_list.size(); gg++) {
                    GapPoint gap = cross_list.get(gg);
                    Point pos_1 = dim_1.get(gap.x - 1);
                    Point pos_2 = dim_2.get(gap.y - 1);
                    Point point = lineLineIntersection(cur_pos_1, cur_pos_2, pos_1, pos_2);
                    if (check_point != null) {
                        if (point.x == check_point.x && point.y == check_point.y) {
                            remove_index_list.add(gg); // remove same position gaps
                        }
                    }
                    cross_list.get(gg).cross_point = point;
                    check_point = point;
                }
                cross_list.removeAll(remove_index_list);
//                for (int index : remove_index_list) {
//                    cross_list.removeAll(remove_index_list);
//                }
                //sort gaps
                Comparator<GapPoint> comparator = Comparator.comparingDouble(GapPoint::getCross);
                cross_list.sort(comparator);
                if (k == 2 && i == 1) {
                    int t = 0;
                }
                for (int gg = 0; gg < cross_list.size(); gg++) {
                    GapPoint gap = cross_list.get(gg);
                    Point point = gap.cross_point;
                    if (point.x == Double.MAX_VALUE
                            && point.y == Double.MAX_VALUE) {
                        continue;
                    }
                    if (prev_point == null) {
                        double x_offset = point.x - cur_pos_2.x;
                        if (x_offset == 0) {
                            double len = Math.abs(cur_pos_2.y - point.y);
                            gap_list.add(len);
                        } else {
                            double len = Math.abs((double) x_offset / (Math.cos(Math.toRadians(angle))));
                            gap_list.add(len);
                        }
                    } else {
                        double x_offset = point.x - prev_point.x;
                        if (x_offset == 0) {
                            double len = Math.abs(prev_point.y - point.y) - 5;
                            gap_list.add(len);
                        } else {
                            double len = Math.abs((double) x_offset / (Math.cos(Math.toRadians(angle)))) - 5;
                            gap_list.add(len);
                        }
                    }
                    prev_point = point;
                }
                double len = 0;

                double x_offset = prev_point.x - cur_pos_1.x;
                if (x_offset == 0) {
                    len = Math.abs(prev_point.y - cur_pos_1.y);
                } else {
                    len = Math.abs((double) x_offset / (Math.cos(Math.toRadians(angle))));
                }
                double[] dash_array = new double[gap_list.size() * 2 + 1];
                for (int g = 0; g < gap_list.size(); g++) {
                    if (gap_list.get(g) < 5) {
                        dash_array[2 * g] = gap_list.get(g) - 1;
                        dash_array[2 * g + 1] = 1;
                    } else {
                        dash_array[2 * g] = gap_list.get(g) - 5;
                        dash_array[2 * g + 1] = 10;
                    }
                }
                dash_array[gap_list.size() * 2] = len;

                gc.setLineDashes(dash_array);
                gc.strokeLine(dim_2.get(edge.getTo() - 1).x, dim_2.get(edge.getTo() - 1).y,
                        dim_1.get(k).x, dim_1.get(k).y);

                gc.setLineDashes(null);
            }
        }
    }

    private void canvasEventFiltering() {
        graph_view.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            boolean shouldDraw = false;
            double dX, dY;
            double range_x_1 = -1;
            double range_x_2 = -1;
            int dim_index = -1;

            boolean top_selection_flag = false;
            boolean bot_Selection_flag = false;

            int top_index = 0;
            int bot_index = 0;

            @Override
            public void handle(MouseEvent arg0) {
                if (arg0.isPrimaryButtonDown()) {
                    if (arg0.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        dX = arg0.getX();
                        dY = arg0.getY();
                        dim_index = getEllipseIndex(dX, dY);

                    } else if (arg0.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                        if (dX <= (radius / 2) || dX >= graph_view.getWidth() - (radius / 2)) {
                            return;
                        }
                        if (dim_index != -1) {
                            if (dim_index >= dim_1.size()) {
                                int dim2_index = dim_index - dim_1.size();
                                dim_2.get(dim2_index).x = dX;
                                dim_index = getChangedIndex(dX, false) + dim_1.size();
                            } else {
                                dim_1.get(dim_index).x = dX;
                                dim_index = getChangedIndex(dX, true);
                            }
                            //sortDims();
                            drawGraphForDrag();
                        }
                        dX = arg0.getX();
                        dY = arg0.getY();
                    } else if (arg0.getEventType() == MouseEvent.MOUSE_RELEASED) {
                        dim_index = -1;
                    }
                }
                if (arg0.isSecondaryButtonDown()) {
                    if (arg0.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        dX = arg0.getX();
                        dY = arg0.getY();
                        dim_index = getEllipseIndex(dX, dY);
                        if (dim_index != -1) {
                            if (dim_index >= dim_1.size()) {
                                int dim2_index = dim_index - dim_1.size();

                                gc.setFill(Color.GREEN);
                                Point point = dim_2.get(dim2_index);
                                gc.fillOval(point.x - (radius / 2), y_bound_1 - (radius / 2), radius, radius);

                                top_selection_flag = true;
                                top_index = dim2_index;
                            } else {
                                gc.setFill(Color.GREEN);
                                Point point = dim_1.get(dim_index);
                                gc.fillOval(point.x - (radius / 2), y_bound_2 - (radius / 2), radius, radius);

                                bot_Selection_flag = true;
                                bot_index = dim_index;
                            }
                            if (top_selection_flag && bot_Selection_flag) {                              
                                allow_lines[bot_index][top_index] = 1;
                                drawGraphForDrag();
                                top_selection_flag = !top_selection_flag;
                                bot_Selection_flag = !bot_Selection_flag;
                                dim_index = -1;
                                
                                deleted_lines.add(new Line(top_index, bot_index));
                            }
                        }
                    } else if (arg0.getEventType() == MouseEvent.MOUSE_RELEASED) {
                        dim_index = -1;
//                     
//                        }
                    }
                }
            }
        });
    }

    private int getChangedIndex(double x, boolean layer) {
        if (layer) {
            Collections.sort(this.dim_1, new PointCompare());
            for (int i = 0; i < dim_1.size(); i++) {
                if (dim_1.get(i).x == x) {
                    return i;
                }
            }
        } else {
            Collections.sort(this.dim_2, new PointCompare());
            for (int i = 0; i < dim_2.size(); i++) {
                if (dim_2.get(i).x == x) {
                    return i;
                }
            }
        }
        return 0;
    }

    private void sortDims() {
        if (dim_1.isEmpty() || dim_2.isEmpty()) {
            return;
        }
        for (int i = 0; i < dim_1.size() - 1; i++) {
            for (int j = i + 1; j < dim_1.size(); j++) {
                Point temp = dim_1.get(j);
                if (dim_1.get(i).x > dim_1.get(j).x) {
                    dim_1.set(j, dim_1.get(i));
                    dim_1.set(i, temp);
                }
            }
        }
        for (int i = 0; i < dim_2.size() - 1; i++) {
            for (int j = i + 1; j < dim_2.size(); j++) {
                Point temp = dim_2.get(j);
                if (dim_2.get(i).x > dim_2.get(j).x) {
                    dim_2.set(j, dim_2.get(i));
                    dim_2.set(i, temp);
                }
            }
        }
    }

    private int checkpointInEllipse(double h, double k, double a, double b, double x, double y) {
        int p = ((int) Math.pow((x - h), 2) / (int) Math.pow(a, 2))
                + ((int) Math.pow((y - k), 2) / (int) Math.pow(b, 2));
        return p;
    }

    private int getEllipseIndex(double x, double y) {
        if (dim_1.isEmpty() || dim_2.isEmpty()) {
            return -1;
        }
        int index = 0;
        for (Point point : dim_1) {
            int pp = checkpointInEllipse(point.x, point.y, radius / 2, radius / 2, x, y);
            if (pp <= 1) {
                return index;
            }
            index++;
        }
        for (Point point : dim_2) {
            int pp = checkpointInEllipse(point.x, point.y, radius / 2, radius / 2, x, y);
            if (pp <= 1) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public double getAngle(Point target, Point source) {
        double angle = (float) Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private Point lineLineIntersection(Point A, Point B, Point C, Point D) {
        // Line AB represented as a1x + b1y = c1 
        double a1 = B.y - A.y;
        double b1 = A.x - B.x;
        double c1 = a1 * (A.x) + b1 * (A.y);

        // Line CD represented as a2x + b2y = c2 
        double a2 = D.y - C.y;
        double b2 = C.x - D.x;
        double c2 = a2 * (C.x) + b2 * (C.y);

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) {
            // The lines are parallel. This is simplified 
            // by returning a pair of FLT_MAX 
            return new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        } else {
            double x = (b2 * c1 - b1 * c2) / determinant;
            double y = (a1 * c2 - a2 * c1) / determinant;
            return new Point(x, y);
        }
    }

    public ArrayList<Point> getIntersection(double x1, double x2, double y1, double y2, double midX, double midY, double h, double v) {
        ArrayList<Point> points = new ArrayList();

        x1 -= midX;
        y1 -= midY;

        x2 -= midX;
        y2 -= midY;

        if (x1 == x2) {
            double y = (v / h) * Math.sqrt(h * h - x1 * x1);
            if (Math.min(y1, y2) <= y && y <= Math.max(y1, y2)) {
                points.add(new Point(x1 + midX, y + midY));
            }
            if (Math.min(y1, y2) <= -y && -y <= Math.max(y1, y2)) {
                points.add(new Point(x1 + midX, -y + midY));
            }
        } else {
            double a = (y2 - y1) / (x2 - x1);
            double b = (y1 - a * x1);

            double r = a * a * h * h + v * v;
            double s = 2 * a * b * h * h;
            double t = h * h * b * b - h * h * v * v;

            double d = s * s - 4 * r * t;

            if (d > 0) {
                double xi1 = (-s + Math.sqrt(d)) / (2 * r);
                double xi2 = (-s - Math.sqrt(d)) / (2 * r);

                double yi1 = a * xi1 + b;
                double yi2 = a * xi2 + b;

                if (isPointInLine(x1, x2, y1, y2, xi1, yi1)) {
                    points.add(new Point(xi1 + midX, yi1 + midY));
                }
                if (isPointInLine(x1, x2, y1, y2, xi2, yi2)) {
                    points.add(new Point(xi2 + midX, yi2 + midY));
                }
            } else if (d == 0) {
                double xi = -s / (2 * r);
                double yi = a * xi + b;

                if (isPointInLine(x1, x2, y1, y2, xi, yi)) {
                    points.add(new Point(xi + midX, yi + midY));
                }
            }
        }

        return points;
    }

    public boolean isPointInLine(double x1, double x2, double y1, double y2, double px, double py) {
        double xMin = Math.min(x1, x2);
        double xMax = Math.max(x1, x2);

        double yMin = Math.min(y1, y2);
        double yMax = Math.max(y1, y2);

        return (xMin <= px && px <= xMax) && (yMin <= py && py <= yMax);
    }
    
    //Here index array(of length equal to length of d array) contains the numbers from 0 to length of d array   
    public Integer [] SortWithIndex(double[] data, Integer[] dims, Integer[] index)
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
}
