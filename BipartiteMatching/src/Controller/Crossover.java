/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

//Crossover data structure
public class Crossover {
    public int vertex_counter;
    
    public static Crossover getInstance(){
        return new Crossover();
    }
    //get number of crossovers
    public int getCrossCounter(){
        int counter = 0;
        for(int i = 1; i < vertex_counter; i++){
            counter += i;
        }
        return counter;
    }
}
