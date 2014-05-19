package com.demo.app.web;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Kunta L.
 *
 */
public class TabsetBean {
    private static final int NUMBER_OF_TABS = 2; // simple to change to add more default tabs
    
    private List<Tab> tabs = new ArrayList<Tab>(NUMBER_OF_TABS);
    
    public TabsetBean() {
        // Loop and add default tabs with generic labels and content
        Tab toAdd;
        for (int i = 0; i < NUMBER_OF_TABS; i++) {
            toAdd = new Tab("Label " + (i+1),
                            "Content " + (i+1));
                            
            tabs.add(toAdd);
        }
    }
    
    public List<Tab> getTabs() {
        return tabs;
    }
    
    public void setTabs(List<Tab> tabs) {
        this.tabs = tabs;
    }
}

