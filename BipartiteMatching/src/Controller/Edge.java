/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;
import java.util.List;

//Edge data structure
public class Edge {
    public int from;
    public int to;
    public int gap;
    public List<GapPoint> cross_list = new ArrayList<GapPoint>();
    
    public Edge(int from, int to, int gap){
        this.from = from;
        this.to = to;
        this.gap = gap;
    }
    public int getFrom(){
        return from;
    }
    public int getTo(){
        return to;
    }
    public int getGap(){
        return gap;
    }
    public void setGap(int gap){
        this.gap = gap;
    }
}
