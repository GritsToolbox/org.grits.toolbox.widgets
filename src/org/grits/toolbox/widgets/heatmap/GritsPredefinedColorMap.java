package org.grits.toolbox.widgets.heatmap;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.eclipse.swt.graphics.RGB;

public enum GritsPredefinedColorMap {
	BlueRedYelow ("BlueRedYellow", new double[] {0, 0.365, 0.746, 1},
			new RGB[]{new RGB(0,0,0), new RGB(0, 0, 255), new RGB(255, 0, 0), new RGB (255, 255, 0)});
	String name;
	double[] values;
	RGB[] colors;
	private GritsPredefinedColorMap(String name, double[] values, RGB[] colors){
		this.name = name;
		this.values = values;
		this.colors = colors;
	}
	
	public LinkedHashMap<Double, RGB> getMap() {
		LinkedHashMap<Double, RGB> map = new LinkedHashMap<Double, RGB>();
		for(int i=0; i<values.length; i++){
			map.put(values[i], colors[i]);
		}
		return map;
	}
	
	public static String[] getStringValues(){
		String[] result = new String[values().length];
		int i =0;
		for(GritsPredefinedColorMap m : values())
			result[i++] = m.name;
		return result;
	}
	
	public static int toIndex(GritsPredefinedColorMap p){
		return Arrays.asList(values()).indexOf(p);
	}
	
	public static GritsPredefinedColorMap fromIndex(int index){
		return Arrays.asList(values()).get(index);
	}
	
	
	@Override
	public String toString() {
		return name;
	}
}