package com.example.demo;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class AdminObject{
    private List<String> top10Searches;
    private ArrayList<String> barrels;
    private ArrayList<String> downloaders;


    public AdminObject(List<String> top10Searches, ArrayList<String> barrels, ArrayList<String> downloaders) {
        this.top10Searches = top10Searches;
        this.barrels = barrels;
        this.downloaders = downloaders;
    }


    public List<String> getTop10Searches() {
        return top10Searches;
    }


    public void setTop10Searches(List<String> top10Searches) {
        this.top10Searches = top10Searches;
    }


    public ArrayList<String> getBarrels() {
        return barrels;
    }


    public void setBarrels(ArrayList<String> barrels) {
        this.barrels = barrels;
    }


    public ArrayList<String> getDownloaders() {
        return downloaders;
    }


    public void setDownloaders(ArrayList<String> downloaders) {
        this.downloaders = downloaders;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
