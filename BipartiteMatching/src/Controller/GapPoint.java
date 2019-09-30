/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

//Gaps point data structure
public class GapPoint {
    public int x;
    public int y;
    public Point cross_point;
    public GapPoint(int x, int y){
        this.x = x;
        this.y = y;
    }
    public double getCross(){
        return cross_point.y;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
}
