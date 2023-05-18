package com.example.demo;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdminObject implements Serializable {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((top10Searches == null) ? 0 : top10Searches.hashCode());
        result = prime * result + ((barrels == null) ? 0 : barrels.hashCode());
        result = prime * result + ((downloaders == null) ? 0 : downloaders.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdminObject other = (AdminObject) obj;
        if (top10Searches == null) {
            if (other.top10Searches != null)
                return false;
        } else if (!top10Searches.equals(other.top10Searches))
            return false;
        if (barrels == null) {
            if (other.barrels != null)
                return false;
        } else if (!barrels.equals(other.barrels))
            return false;
        if (downloaders == null) {
            if (other.downloaders != null)
                return false;
        } else if (!downloaders.equals(other.downloaders))
            return false;
        return true;
    }
}
