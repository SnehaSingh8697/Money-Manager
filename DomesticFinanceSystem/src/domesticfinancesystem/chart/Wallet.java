package domesticfinancesystem.chart;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sneha
 */
public class Wallet
{
   private int ID; 
   private String name; 

    public Wallet(int ID, String name)
    {
        this.ID = ID;
        this.name = name;
    }

    public int getID()
    {
        return ID;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name ;
    }
   
}
