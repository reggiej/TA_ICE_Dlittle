package com.demo.app.web;

public class SelectRadioItem<T> {
	/**
	 * @author Kunta L.
	 *
	 */
private boolean selected;
	
	private T value;
	
	public SelectRadioItem (T value){
		this.value = value;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(T value) {
		this.value = value;
	}
}
