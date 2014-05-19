package com.demo.app.web;

/**
 * @author Kunta L.
 *
 */
public class Tab
{
    private String label;
    private String content;
    
    public Tab() {
        label = "Default Label";
        content = "Default Content";
    }
    
    public Tab(String label) {
        this.label = label;
        content = "Default Content";
    }
    
    public Tab(String label, String content) {
        this.label = label;
        this.content = content;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}

