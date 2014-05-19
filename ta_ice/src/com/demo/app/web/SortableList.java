package com.demo.app.web;
/**
 *
 * @author Kunta L.
 *
 */
public abstract class SortableList extends BaseUIBean {
	
	protected static String sortColumnName;
    protected boolean ascending;
    protected String serviceName;

    // we only want to resort if the older or column has changed.
    protected String oldSort;
    protected boolean oldAscending;


    protected SortableList(String defaultSortColumn) {
        sortColumnName = defaultSortColumn;
        ascending = isDefaultAscending(defaultSortColumn);
        oldSort = sortColumnName;
        // make sure sortColumnName on first render
        oldAscending = !ascending;
    }

	@Override
	public String getBeanName() {
		// TODO Auto-generated method stub
		return null;
	}
	
    /**
     * Sort the list.
     */
    protected abstract void sort();

    /**
     * Is the default sortColumnName direction for the given column "ascending" ?
     */
    protected abstract boolean isDefaultAscending(String sortColumn);
    
    /**
     * Gets the sortColumnName column.
     *
     * @return column to sortColumnName
     */
    public String getSortColumnName() {
        return sortColumnName;
    }

    /**
     * Sets the sortColumnName column
     *
     * @param sortColumnName column to sortColumnName
     */
    public void setSortColumnName(String sortColumnName) {
        oldSort = SortableList.sortColumnName;
        SortableList.sortColumnName = sortColumnName;

    }

    /**
     * Is the sortColumnName ascending.
     *
     * @return true if the ascending sortColumnName otherwise false.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Set sortColumnName type.
     *
     * @param ascending true for ascending sortColumnName, false for desending sortColumnName.
     */
    public void setAscending(boolean ascending) {
        oldAscending = this.ascending;
        this.ascending = ascending;
    }

    
}
