package com.demo.app.web;

import java.io.Serializable;

import javax.faces.event.AbortProcessingException;

import com.icesoft.faces.component.panelpositioned.PanelPositionedEvent;
import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.component.paneltabset.TabChangeListener;

public class StaticPanelBean implements TabChangeListener, Serializable {

	    /**
	 * @author Kunta L.
	 */
	
	private static final long serialVersionUID = -6634534387537484225L;
		/**
	     * The demo contains three tabs and thus we need three variables to store
	     * their respective rendered states.
	     */
	    private boolean Panel1Visible;
	    private boolean Panel2Visible;
	    private boolean Panel3Visible;
	    private boolean Panel4Visible;
	    private boolean Panel5Visible;
	    private boolean Panel6Visible;
	    private boolean Panel7Visible;
	    private boolean Panel8Visible;
	    private boolean Panel9Visible;
	    private boolean Panel10Visible;
	    private boolean Panel11Visible;
	    private boolean Panel12Visible;
	    private boolean Panel13Visible;
	    
	    
		// selected tab index
	    private String selectedIndex = "0";

	    /**
	     * panel placement, possible values are "top" and "bottom", the default is
	     * "bottom".
	     */
	    private String panelPlacement;
	   
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
	    public void processPanelChange(PanelPositionedEvent panelChangeEvent)
	            throws AbortProcessingException {
	        // only used to show panelChangeListener usage.
	    }
	    
	    public boolean isPanel1Visible() {
	        return Panel1Visible;
	    }

	    public void setPanel1Visible(boolean Panel1Visible) {
	        this.Panel1Visible = Panel1Visible;
	    }

	    public boolean isPanel2Visible() {
	        return Panel2Visible;
	    }

	    public void setPanel2Visible(boolean Panel2Visible) {
	        this.Panel2Visible = Panel2Visible;
	    }

	    public String getTabPlacement() {
	        return panelPlacement;
	    }

	    public void setTabPlacement(String tabPlacement) {
	        this.panelPlacement = tabPlacement;
	    }
	    
	    public boolean isPanel3Visible() {
			return Panel3Visible;
		}

		public void setPanel3Visible(boolean Panel3Visible) {
			this.Panel3Visible = Panel3Visible;
		}

		public boolean isPanel4Visible() {
			return Panel4Visible;
		}

		public void setPanel4Visible(boolean Panel4Visible) {
			this.Panel4Visible = Panel4Visible;
		}

		public boolean isPanel5Visible() {
			return Panel5Visible;
		}

		public void setPanel5Visible(boolean Panel5Visible) {
			this.Panel5Visible = Panel5Visible;
		}

		public boolean isPanel6Visible() {
			return Panel6Visible;
		}

		public void setPanel6Visible(boolean Panel6Visible) {
			this.Panel6Visible = Panel6Visible;
		}

		public boolean isPanel7Visible() {
			return Panel7Visible;
		}

		public void setPanel7Visible(boolean Panel7Visible) {
			this.Panel7Visible = Panel7Visible;
		}

		public boolean isPanel8Visible() {
			return Panel8Visible;
		}

		public void setPanel8Visible(boolean Panel8Visible) {
			this.Panel8Visible = Panel8Visible;
		}

		public boolean isPanel9Visible() {
			return Panel9Visible;
		}

		public void setPanel9Visible(boolean Panel9Visible) {
			this.Panel9Visible = Panel9Visible;
		}

		public boolean isPanel10Visible() {
			return Panel10Visible;
		}

		public void setPanel10Visible(boolean Panel10Visible) {
			this.Panel10Visible = Panel10Visible;
		}

		public boolean isPanel11Visible() {
			return Panel11Visible;
		}

		public void setPanel11Visible(boolean Panel11Visible) {
			this.Panel11Visible = Panel11Visible;
		}

		public boolean isPanel12Visible() {
			return Panel12Visible;
		}

		public void setPanel12Visible(boolean Panel12Visible) {
			this.Panel12Visible = Panel12Visible;
		}

		public boolean isPanel13Visible() {
			return Panel13Visible;
		}

		public void setPanel13Visible(boolean Panel13Visible) {
			this.Panel13Visible = Panel13Visible;
		}

		public static long getSerialVersionUID() {
			return serialVersionUID;
		}

		public void setPanelPlacement(String panelPlacement) {
			this.panelPlacement = panelPlacement;
		}

		public String getPanelPlacement() {
			return panelPlacement;
		}

	
		public void processTabChange(TabChangeEvent arg0)
				throws AbortProcessingException {
			// TODO Auto-generated method stub
			
		}

	}