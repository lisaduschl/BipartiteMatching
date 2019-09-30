/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;
import java.util.List;

//Edge data structure
public class Line {
    public int from;
    public int to;
    
    public Line(int from, int to){
        this.from = from;
        this.to = to;
    }
    
    public boolean isSameTo(Line other){
        if ((this.from == other.from && this.to == other.to) || (this.from == other.to && this.to == other.from))
            return true;
        else
            return false;
    }
}
