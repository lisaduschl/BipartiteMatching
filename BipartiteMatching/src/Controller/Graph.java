/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Controller.Edge;
import Controller.GapPoint;
import Controller.Line;
import View.AlertMessage;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates the Graph
 *
 */
public class Graph {

     
    public int vertex_num_1 = 0; // top vertex number
    public int vertex_num_2 = 0; // bottom vertex number
    public int max_gap_num = 0; // maximum gap number
    public int[][] allow_lines; 

    public List<List<Edge>> sets = new ArrayList<List<Edge>>(); //set of all edges
    public List<List<Edge>> buf_sets = new ArrayList<List<Edge>>(); //temporary for edge sets
    
    public List<Line> all_edges = new ArrayList<Line>(); // all edges of graph

    /**
     * Construct initial edges
     */
    public void constructGraph() {
        sets.clear();
        all_edges.clear();
        
        for (int i = 0; i < vertex_num_1; i++) {
            for (int j = 0; j < vertex_num_2; j++) {
                //if (!(i == 0 && j == 0) && !(i == vertex_num_1 - 1 && j == vertex_num_2 - 1))
                    all_edges.add(new Line(i, j));
            }
        }
        
        for (int i = 1; i <= vertex_num_2; i++) {
            List<Edge> set_one = new ArrayList<Edge>();
            //set possible numbers of edges
            if (i == 1) {
                for (int j = 1; j <= vertex_num_1; j++) {
                    if(allow_lines[i - 1][j - 1] == 1){
                        Edge edge = new Edge(i, j, -3);
                        set_one.add(edge);
                        continue;
                    }
                    Edge edge = new Edge(i, j, 0); // first node is gaps 0
                    if (j == 1) {
                        edge.setGap(-1); // end is gaps -1
                    }
                    set_one.add(edge);
                }
            } else if (i >= 2) {
                for (int j = 1; j <= vertex_num_1; j++) {
                    if(allow_lines[i - 1][j - 1] == 1){
                        Edge edge = new Edge(i, j, -3);
                        set_one.add(edge);
                        continue;
                    }
                    int gaps = (vertex_num_1 - j) * (i - 1);
                    gaps = gaps - getGaps(i, j);
                    Edge edge = new Edge(i, j, gaps);
                    createCrossOver(edge, i, j); //get possible crossover number for each edge
                    //add crossover list to each
                    if (i == vertex_num_2 && j == vertex_num_1) {
                        edge.setGap(-1);
                        edge.cross_list.clear();
                    }
                    set_one.add(edge);
                }
            }
            sets.add(set_one);
        }
    }
    /**
     * construct graph for check
     * This is for getting minimum gaps number and for other checking
     */
    public void constructGraphForCheck() {
        sets.clear();
        for (int i = 1; i <= vertex_num_2; i++) {
            List<Edge> set_one = new ArrayList<Edge>();
            List<Edge> buf_set_one = new ArrayList<Edge>();
            if (i == 1) {
                for (int j = 1; j <= vertex_num_1; j++) {
                    Edge edge = new Edge(i, j, 0); // first node is gaps 0
                    Edge buf_edge = new Edge(i, j, 0); // first node is gaps 0
                    if (j == 1) {
                        edge.setGap(-1); // end is gaps -1
                        buf_edge.setGap(-1); // end is gaps -1
                    }
                    set_one.add(edge);
                    buf_set_one.add(buf_edge);
                }
            } else if (i >= 2) {
                for (int j = 1; j <= vertex_num_1; j++) {
                    Edge edge = new Edge(i, j, (vertex_num_1 - j) * (i - 1));
                    Edge buf_edge = new Edge(i, j, (vertex_num_1 - j) * (i - 1));
                    createCrossOver(edge, i, j);
                    createCrossOver(buf_edge, i, j);
                    //add crossover list ot each
                    if (i == vertex_num_2 && j == vertex_num_1) {
                        edge.setGap(-1);
                        edge.cross_list.clear();
                        buf_edge.setGap(-1);
                        buf_edge.cross_list.clear();
                    }
                    set_one.add(edge);
                    buf_set_one.add(buf_edge);
                }
            }
            sets.add(set_one);
            buf_sets.add(buf_set_one);
        }
    }
    //get gaps number between two vertices
    public int getGaps(int vert2_index, int vert1_index){
        int remove_counter = 0;
        for(int i = 0; i < vert2_index - 1; i++){
            for(int j = vert1_index; j < vertex_num_1; j++){
                if(allow_lines[i][j] == 1){
                    remove_counter++;
                }
            }
        }
        return remove_counter;
    }
    //processing algorithm
    public void processingBPM() {
        if (allow_lines[vertex_num_2 - 1][0] != 1) { //added
            if (sets.get(sets.size() - 1).get(0).getGap() <= max_gap_num) {
                return;
            }
        } //added
        for (int m = 0; m < vertex_num_2; m++) {
            for (int j = 0; j < vertex_num_1; j++) {
                if (sets.get(m).get(j).getGap() <= max_gap_num) {
                    continue;
                }
                if(m == 4){
                    int t = 0; 
                }
                //get free gaps
                int gaps = sets.get(m).get(j).getGap();
                int free_gaps = gaps - max_gap_num;
                //check whether this edge is possible to draw now or not
                if (getAllowAllocate(j, m, free_gaps)) {
                    sets.get(m).get(j).setGap(max_gap_num);
                    //allocate gaps to other edges
                    int counter = 0;
                    boolean full_option = false;
                    for (int n = 0; n <= m - 1; n++) {
                        if (full_option) {
                            break;
                        }
                        for (int k = j + 1; k < vertex_num_1; k++) {
                            if (counter == free_gaps) {
                                full_option = true;
                                break;
                            }
                            int current_gap = sets.get(n).get(k).getGap();
                            if (current_gap >= max_gap_num) {
                                continue;
                            }
                            if (current_gap < 0) {
                                removeCross(sets.get(m).get(j), n + 1, k + 1); //remove cross
                                continue;
                            }
                            //allocate gaps to other edges and remove its gaps now
                            sets.get(n).get(k).setGap(current_gap + 1);
                            addCross(sets.get(n).get(k), m + 1, j + 1); // add cross
                            removeCross(sets.get(m).get(j), n + 1, k + 1); //remove cross
                            counter++;
                        }
                    }
                } else {
                    sets.get(m).get(j).setGap(-2);
                    sets.get(m).get(j).cross_list.clear();
                }
            }
        }
    }
    /**
     * Check if this edge is possible to draw or not
     * @param top
     * @param bot
     * @param free_num
     * @return 
     */
    public boolean getAllowAllocate(int top, int bot, int free_num) {
        int available_num = 0;
        for (int k = 0; k <= bot - 1; k++) {
            for (int i = top + 1; i < vertex_num_1; i++) {
                int gaps = sets.get(k).get(i).getGap();
                if(gaps < 0) continue;
                if (max_gap_num > gaps) {
                    available_num ++;
                }
            }
        }
        if (free_num <= available_num) {
            return true;
        }
        return false;
    }
    /**
     * get all possible crossover numbers
     * @param edge
     * @param bot_index
     * @param top_index 
     */
    private void createCrossOver(Edge edge, int bot_index, int top_index) {
        for (int i = 1; i <= bot_index - 1; i++) {
            for (int j = top_index + 1; j <= vertex_num_1; j++) {
                if(allow_lines[i - 1][j - 1] == 1) continue;
                GapPoint point = new GapPoint(i, j);
                edge.cross_list.add(point);
            }
        }
    }
    /**
     * Remove crossover between two vertices from this edge
     * @param edge
     * @param bot_index
     * @param top_index 
     */
    private void removeCross(Edge edge, int bot_index, int top_index) {
        List<GapPoint> cross_list = edge.cross_list;
        if (cross_list.isEmpty()) {
            return;
        }
        //find index
        int size = cross_list.size();
        int index = -1;
        for (int i = 0; i < size; i++) {
            GapPoint point = cross_list.get(i);
            if (point.x == bot_index && point.y == top_index) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            edge.cross_list.remove(index);
        }
    }
    //add cross over to edge
    private void addCross(Edge edge, int bot_index, int top_index) {
        edge.cross_list.add(new GapPoint(bot_index, top_index));
    }
    //check edge is possible to draw or not
    private boolean checkAllow(){
        if (sets.get(sets.size() - 1).get(0).getGap() <= max_gap_num) {
            return true;
        }
        for (int m = 0; m < vertex_num_2; m++) {
            for (int j = 0; j < vertex_num_1; j++) {
                if (sets.get(m).get(j).getGap() <= max_gap_num) {
                    continue;
                }
                //get free gaps
                int gaps = sets.get(m).get(j).getGap();
                int free_gaps = gaps - max_gap_num;
                if (getAllowAllocate(j, m, free_gaps)) {
                    sets.get(m).get(j).setGap(max_gap_num);
                    //allocate gaps to other
                    int counter = 0;
                    boolean full_option = false;
                    for (int n = 0; n <= m - 1; n++) {
                        if (full_option) {
                            break;
                        }
                        for (int k = j + 1; k < vertex_num_1; k++) {
                            if (counter == free_gaps) {
                                full_option = true;
                                break;
                            }
                            int current_gap = sets.get(n).get(k).getGap();
                            if (current_gap >= max_gap_num) {
                                continue;
                            }
                            if (current_gap < 0) {
                                continue;
                            }
                            sets.get(n).get(k).setGap(current_gap + 1);
                            counter++;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    //check full connection of this graph
    public boolean checkFullOption(){
        constructGraphForCheck();
        return checkAllow();
    }
    //get minumum gaps number that allow full connection
    public int getSmallestGaps(){
        int buf_gaps = max_gap_num;
        int smallest = 1;
        constructGraphForCheck();
        
        while(true){
            max_gap_num = smallest;
            if(checkAllow()) break;
            smallest++;
            copySets();
        }
        max_gap_num = buf_gaps;
        return smallest;
    }
    private void copySets(){
        sets.clear();
        for(List<Edge> item : buf_sets){
            List<Edge> first_list = new ArrayList<Edge>();
            for(Edge edge_item : item){
                Edge edge = new Edge(edge_item.getFrom(), edge_item.getTo(), edge_item.getGap());
                first_list.add(edge);
            }
            sets.add(first_list);
        }
    }
    
    public int includeEdge(Line other) {
        for (int i = 0; i < all_edges.size(); i++) {
            if ((all_edges.get(i).from == other.from && all_edges.get(i).to == other.to)) {
                return i;
            }
        }
        return -1;
    }
}
