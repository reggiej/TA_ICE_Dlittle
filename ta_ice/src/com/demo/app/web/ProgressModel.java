/**
 * 
 */
package com.demo.app.web;

/**
 * @author 
 *
 */
public class ProgressModel {

	static private String DEFAULT_LABEL_POSITION = "embed";
	
	private int percentComplete;
	
	private String labelPosition;
	
	private String label;
	
	private String labelComplete;
	
	public ProgressModel (){
		labelPosition = DEFAULT_LABEL_POSITION;
	}

	/**
	 * @return the percentComplete
	 */
	public int getPercentComplete() {
		return percentComplete;
	}

	/**
	 * @param percentComplete the percentComplete to set
	 */
	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	/**
	 * @return the labelPosition
	 */
	public String getLabelPosition() {
		return labelPosition;
	}

	/**
	 * @param labelPosition the labelPosition to set
	 */
	public void setLabelPosition(String labelPosition) {
		this.labelPosition = labelPosition;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the labelComplete
	 */
	public String getLabelComplete() {
		return labelComplete;
	}

	/**
	 * @param labelComplete the labelComplete to set
	 */
	public void setLabelComplete(String labelComplete) {
		this.labelComplete = labelComplete;
	}
	
	
}
