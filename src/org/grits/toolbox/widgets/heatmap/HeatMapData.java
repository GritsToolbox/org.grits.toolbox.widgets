package org.grits.toolbox.widgets.heatmap;

public class HeatMapData {
	
	String rowIdentifier;
	String columnIdentifier;
	
	Double value;

	public String getRowIdentifier() {
		return rowIdentifier;
	}

	public void setRowIdentifier(String rowIdentifier) {
		this.rowIdentifier = rowIdentifier;
	}

	public String getColumnIdentifier() {
		return columnIdentifier;
	}

	public void setColumnIdentifier(String columnIdentifier) {
		this.columnIdentifier = columnIdentifier;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
	
}
