package com.demo.app.web;

import java.io.Serializable;

import javax.faces.event.AbortProcessingException;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.component.paneltabset.TabChangeListener;

public class StaticTabSetBean implements TabChangeListener, Serializable {

	/**
	 * @author Kunta L.
	 *
	 */
	private static final long serialVersionUID = 4688295975809765043L;
		/**
	     * The demo contains three tabs and thus we need three variables to store
	     * their respective rendered states.
	     */
	    private boolean tabbedPane1Visible;
	    private boolean tabbedPane2Visible;

	    // selected tab index
	    private String selectedIndex = "0";

	    /**
	     * Tabbed placement, possible values are "top" and "bottom", the default is
	     * "bottom".
	     */
	    private String tabPlacement = "top";

	    public boolean isTabbedPane1Visible() {
	        return tabbedPane1Visible;
	    }

	    public void setTabbedPane1Visible(boolean tabbedPane1Visible) {
	        this.tabbedPane1Visible = tabbedPane1Visible;
	    }

	    public boolean isTabbedPane2Visible() {
	        return tabbedPane2Visible;
	    }

	    public void setTabbedPane2Visible(boolean tabbedPane2Visible) {
	        this.tabbedPane2Visible = tabbedPane2Visible;
	    }

	    public String getTabPlacement() {
	        return tabPlacement;
	    }

	    public void setTabPlacement(String tabPlacement) {
	        this.tabPlacement = tabPlacement;
	    }

	    public String getSelectedIndex() {
	        return selectedIndex;
	    }

	    public void setSelectedIndex(String selectedIndex) {
	        this.selectedIndex = selectedIndex;
	    }

	    public void setSelectedIndex(int selectedIndex) {
	        this.selectedIndex = String.valueOf(selectedIndex);
	    }

	    public int getFocusIndex() {
	        return Integer.parseInt(selectedIndex);
	    }

	    public void setFocusIndex(int index){
	        selectedIndex = String.valueOf(index);
	    }

	    /**
	     * Called when the table binding's tab focus changes.
	     *
	     * @param tabChangeEvent used to set the tab focus.
	     * @throws AbortProcessingException An exception that may be thrown by event
	     *                                  listeners to terminate the processing of the current event.
	     */
	    public void processTabChange(TabChangeEvent tabChangeEvent)
	            throws AbortProcessingException {
	        // only used to show TabChangeListener usage.
	    }
	}