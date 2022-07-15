package org.grits.toolbox.widgets.heatmap.gui;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap;
import org.eclipse.nebula.visualization.widgets.figureparts.ColorMapRamp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.widgets.heatmap.GritsPredefinedColorMap;

public class ColorMapCanvas extends Canvas {
	
	LightweightSystem lws;
	ColorMapRamp colorMapRamp;
	ColorMap colorMap;
	Double min=0.0;
	Double max = 255.0;
	
	
	public ColorMapCanvas(Composite parent, int style) {
		super(parent, style);
		init();
	}
	
	public void setColorMap(ColorMap colorMap) {
		this.colorMap = colorMap;
	}
	
	public void setMin(Double min) {
		this.min = min;
	}
	
	public void setMax(Double max) {
		this.max = max;
	}

	private void init() {
		lws = new LightweightSystem(); 
		lws.setControl(this);
	}

	public void initializeRamp (Double min, Double max, ColorMap colorMap) {
		colorMapRamp = new ColorMapRamp();
		colorMapRamp.setColorMap(colorMap);
		colorMapRamp.setMin(min);
		colorMapRamp.setMax(max);
		lws.setContents(colorMapRamp);
	}
	
	public static void main(String[] args) {
		final Shell shell = new Shell();
		shell.setSize(100, 200);
		shell.setLayout(new FillLayout());
		shell.open();
		
		ColorMapCanvas colorMapRamp = new ColorMapCanvas(shell, SWT.NONE);
		
		ColorMap colorMap = new ColorMap();
		colorMap.setColorMap(GritsPredefinedColorMap.BlueRedYelow.getMap());
		//colorMap.setAutoScale(true);  // true by default
		//colorMap.setInterpolate(true);
		//colorMapRamp.setColorMap(colorMap);
		//colorMapRamp.setMin(-100.0);
		//colorMapRamp.setMax(100.0);
		
		colorMapRamp.initializeRamp(-100.0, 100.0, colorMap);
		colorMapRamp.setBounds(shell.getClientArea());
		
		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
