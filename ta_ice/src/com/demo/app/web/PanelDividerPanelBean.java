package com.demo.app.web;
/**
 * @author Kunta L.
 * @author Phil P.
 *
 */
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;

public class PanelDividerPanelBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7877838795768545628L;
	/**
	 * Simple model bean for the panelDivider example.  The bean maintains the
	 * position and orientation of the divider.
	 *
	 * @since 1.7
	 */

	    public static final String ORIENTATION_HOR = "horizontal";
	    public static final String ORIENTATION_VER = "vertical";

	    private static final int POSITION_DEFAULT = 40;

	    private String orientation = ORIENTATION_VER;
	    private int position = POSITION_DEFAULT;

	    public String getOrientation() {
	        return orientation;
	    }

	    public int getPosition() {
	        return position;
	    }

	    public void setOrientation(String orientation) {
	        this.orientation = orientation;
	    }

	    public void setPosition(int position) {
	        this.position = position;
	    }

	    /**
	     * Listener method called when the orientation is changed
	     * This is useful to allow us to reset the position of the divider to
	     * the default value
	     *
	     * @param event of the change jsf event
	     */
	    public void orientationChanged(ValueChangeEvent event) {
	        this.position = POSITION_DEFAULT;
	    }
	}

