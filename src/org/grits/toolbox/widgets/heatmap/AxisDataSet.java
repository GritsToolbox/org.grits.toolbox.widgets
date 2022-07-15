package org.grits.toolbox.widgets.heatmap;

import java.util.List;

public class AxisDataSet {
	
	List<String> values;
	
	public void setValues(List<String> values) {
		this.values = values;
	}

	public String getLabel(double min) {
		if (values != null && min < values.size()) {
			int index = (int)Math.floor(min);
			return values.get(index);
		}
		return "";
	}
}
