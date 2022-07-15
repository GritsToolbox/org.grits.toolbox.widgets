package org.grits.toolbox.widgets.heatmap.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.widgets.heatmap.AxisDataSet;
import org.grits.toolbox.widgets.heatmap.GritsPredefinedColorMap;
import org.grits.toolbox.widgets.heatmap.HeatMapData;
import org.grits.toolbox.widgets.heatmap.gui.GritsHeatMapFigure.IROIListener;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

public class HeatMapChart extends Canvas {
	
	static int count = 0;
	
	LightweightSystem lws;
	
	GritsHeatMapFigure heatMap;
	private HeatMapData[][] chartData;   // original full data set
	private HeatMapData[][] ordered;
	private List<String> orderedRowIdentifiers;
	private List<String> orderedColumnIdentifiers;
	private String[] columnIdentifiers;
	private String[] rowIdentifiers;
	private int originaldataWidth;
	private int originaldataHeight;
	int dataWidth;
	int dataHeight;
	
	
	Double min = 0.0;
	Double max = 0.0;

	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> future;
	
	//PREFERENCES
	boolean rowDendrogramVisible=true;
	boolean columnDendrogramVisible=true;
	boolean xAxisVisible= true;
	boolean yAxisVisible=true;
	boolean xAxisOnTop=false;
	boolean yAxisOnRight=true;
	int angle=0;
	ColorMap colorMap;
	DistanceFormula distanceFormula = DistanceFormula.Spherical;
	
	enum DistanceFormula{
		Euclidean ("Euclidean"),
		Spherical ("Spherical");
		
		String label;
		private DistanceFormula(String label) {
			this.label = label;
		}
	}

	public HeatMapChart(Composite parent) {
		super(parent, SWT.NONE);
		init();
	}
	
	public Double getMin() {
		return min;
	}
	
	public Double getMax() {
		return max;
	}
	
	private void init() {
		lws = new LightweightSystem(); 
		lws.setControl(this); 
		
		if (colorMap == null)  {  // default color map
			colorMap = new ColorMap();
			colorMap.setColorMap(GritsPredefinedColorMap.BlueRedYelow.getMap());
			colorMap.setAutoScale(true);
			colorMap.setInterpolate(true);
		}
			
		final MenuManager manager = new MenuManager(); 
		MenuManager mgr = new MenuManager("Test"); 
		/*mgr.add(new ChangeDockAction(PaletteMessages.LEFT_LABEL, 
	     PositionConstants.WEST)); 
		mgr.add(new ChangeDockAction(PaletteMessages.RIGHT_LABEL, 
	     PositionConstants.EAST)); */ 
		manager.add(new Action("Show ColorMap") {
			@Override
			public void run() {
				Shell shell = new Shell();
				shell.setSize(100, 200);
				ColorMapCanvas colorMap = new ColorMapCanvas(shell, SWT.NONE);
				colorMap.initializeRamp(heatMap.getMin(), heatMap.getMax(), heatMap.getColorMap());
				shell.setLayout(new FillLayout());
				shell.open();
				colorMap.setBounds(shell.getClientArea());
				Display display = Display.getCurrent();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		}); 
		
		manager.add(new Action("Show/Hide Row Dendrogram") {
			@Override
			public void run() {
				heatMap.getRowDendrogram().setVisible(!heatMap.getRowDendrogram().isVisible());
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Show/Hide Column Dendrogram") {
			@Override
			public void run() {
				heatMap.getColumnDendrogram().setVisible(!heatMap.getColumnDendrogram().isVisible());
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Show/Hide XAxis") {
			@Override
			public void run() {
				heatMap.getXAxis().setVisible(!heatMap.getXAxis().isVisible());
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Show/Hide YAxis") {
			@Override
			public void run() {
				heatMap.getYAxis().setVisible(!heatMap.getYAxis().isVisible());
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Put XAxis on Top") {
			@Override
			public void run() {
				heatMap.getXAxis().setPrimarySide(false);
				heatMap.setxAxisOnTop(true);
				if (heatMap.getColumnDendrogram().isVisible()) {
					// move it to the bottom
					heatMap.getColumnDendrogram().setPrimarySide(false);
				}
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Put XAxis at the Bottom") {
			@Override
			public void run() {
				heatMap.getXAxis().setPrimarySide(true);
				heatMap.setxAxisOnTop(false);
				if (heatMap.getColumnDendrogram().isVisible()) {
					// move it to the bottom
					heatMap.getColumnDendrogram().setPrimarySide(true);
				}
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Put XAxis on Top") {
			@Override
			public void run() {
				heatMap.getXAxis().setPrimarySide(false);
				heatMap.setxAxisOnTop(true);
				if (heatMap.getColumnDendrogram().isVisible()) {
					// move it to the bottom
					heatMap.getColumnDendrogram().setPrimarySide(false);
				}
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Change XAxis Label Angle") {
			@Override
			public void run() {
				
				Dialog tickLabelAngleDialog = new Dialog(Display.getCurrent().getActiveShell()) {
					private ControlDecoration dec2;
					@Override
					protected Control createDialogArea(Composite parent) {
						Composite container = new Composite(parent, SWT.NONE);
						GridLayout gridLayout = new GridLayout();
						gridLayout.numColumns = 2;
						gridLayout.verticalSpacing = 10;
						container.setLayout(gridLayout);
						Label angleLabel = new Label(container, SWT.LEFT);
						angleLabel.setText("Enter an angle");
						angleLabel.setToolTipText("Angle in which the XAxis labels should be drawn");
						Text angleText = new Text(container, SWT.BORDER);
						angleText.setText("0");
						angleText.setLayoutData(new GridData(50, angleText.getLineHeight()));
						angle = 0;
						
						// Create a control decoration for the control.
						dec2 = new ControlDecoration(angleText, SWT.TOP | SWT.LEFT);
						// Specify the decoration image and description
						Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
						dec2.setImage(image);
						dec2.setDescriptionText("Should enter an integer between [0-90]");
						dec2.hide();
						
						angleText.addModifyListener(new ModifyListener() {

							@Override
							public void modifyText(ModifyEvent e) {
								Text newText = (Text) e.widget;
								String newValue = newText.getText();
								if (newValue != null)
									newValue = newValue.trim();
								try {
									angle = Integer.parseInt(newValue);
									if (angle < 0 || angle > 90) {
										dec2.show();
									} else
										dec2.hide();
								} catch (NumberFormatException ex) {
									dec2.show();
								}
								
							}
						});
						
						return container;
					}
				};
				
				if (tickLabelAngleDialog.open() == Window.OK) {
					((GritsAxis)heatMap.getXAxis()).getTickLabels().setTickLabelAngle(angle);
					heatMap.repaint();
				}
			}
		});
		
		manager.add(new Action("Put YAxis to the Left") {
			@Override
			public void run() {
				heatMap.getYAxis().setPrimarySide(true);
				heatMap.setyAxisOnRight(false);
				if (heatMap.getRowDendrogram().isVisible()) {
					// move it to the right
					heatMap.getRowDendrogram().setPrimarySide(false);
				}
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Put YAxis to the Right") {
			@Override
			public void run() {
				heatMap.getYAxis().setPrimarySide(false);
				heatMap.setyAxisOnRight(true);
				if (heatMap.getRowDendrogram().isVisible()) {
					// move it to the right
					heatMap.getRowDendrogram().setPrimarySide(true);
				}
				heatMap.repaint();
			}
		});
		
		manager.add(new Action("Change Color Map") {
			@Override
			public void run() {
				List<String> colorMapList = new ArrayList<>();
				colorMapList.addAll(Arrays.asList(PredefinedColorMap.getStringValues()));
				colorMapList.add(GritsPredefinedColorMap.BlueRedYelow.name());
				MyListDialog dialog = new MyListDialog(Display.getCurrent().getActiveShell());
				dialog.setLabelProvider(new LabelProvider());
				dialog.setContentProvider(new ArrayContentProvider());
				dialog.setInput(colorMapList);
				dialog.setMessage("Please select the new color map");
				dialog.setInitialSelections(new Object[] {heatMap.getColorMap().getPredefinedColorMap().name()});
				if (dialog.open()==Window.OK) {
					Object[] selected = dialog.getResult();
					if (selected.length > 0) {
						String newColorMap = (String)selected[0];
						if (!newColorMap.equals(heatMap.getColorMap().getPredefinedColorMap().name())) {
							// change the color map
							PredefinedColorMap colorMap = findColorMap (PredefinedColorMap.values(), newColorMap);
							if (colorMap != null) {
								heatMap.setColorMap(new ColorMap(colorMap, true, true));
								heatMap.repaint();
							} else {
								// check our own list
								GritsPredefinedColorMap colorMap1 = GritsPredefinedColorMap.valueOf(newColorMap);
								ColorMap newMap = new ColorMap();
								newMap.setColorMap(colorMap1.getMap());
								newMap.setAutoScale(true);
								newMap.setInterpolate(true);
								heatMap.setColorMap(newMap);
								heatMap.repaint();
							}
						}
					}
				}
				
			}

			private PredefinedColorMap findColorMap(PredefinedColorMap[] values, String newColorMap) {
				for (PredefinedColorMap predefinedColorMap : values) {
					if (predefinedColorMap.name().equals(newColorMap))
						return predefinedColorMap;
				}
				return null;
			}
		});
		manager.add(mgr); 
		setMenu(manager.createContextMenu(this)); 
		mgr.addMenuListener(new IMenuListener() { 
			public void menuAboutToShow(IMenuManager menuMgr) { 
				IContributionItem[] items = menuMgr.getItems(); 
				for (int i = 0; i < items.length; i++) { 
					((ActionContributionItem) items[i]).update(); 
				} 
			} 
	   }); 
	}

	public GritsHeatMapFigure getHeatMap() {
		return heatMap;
	}
	
	public void initializeChart (HeatMapData[][] chartData, String[] columnIndentifiers, String[] rowIdentifiers) throws Exception {
		if (chartData == null || columnIndentifiers==null || rowIdentifiers == null) 
			return;
		
		this.chartData = chartData;
		this.columnIdentifiers = columnIndentifiers;
		this.rowIdentifiers = rowIdentifiers;
	
		originaldataWidth = columnIndentifiers.length;
		originaldataHeight = rowIdentifiers.length;
	
		dataWidth = originaldataWidth;
		dataHeight = originaldataHeight;
		
		if (chartData.length != originaldataHeight)
			throw new Exception ("data values do not match the number of columns");
		if (chartData[0] == null || chartData[0].length != originaldataWidth) {
			throw new Exception ("data values do not match the number of rows");
		}
		
		heatMap = new GritsHeatMapFigure(this);
		cluster (chartData, columnIndentifiers, rowIdentifiers);
	}
	
	public void cluster (HeatMapData[][] chartData, String[] columnIndentifiers, String[] rowIdentifiers) {
		
		double [][] distancesForRows = calculateDistancesForRows(chartData);
		double [][] distancesForColumns = calculateDistancesForColumns(chartData);
		
		Cluster rowCluster = applyClustering(distancesForRows, rowIdentifiers);
		Cluster columnCluster = applyClustering(distancesForColumns, columnIndentifiers);
		
		//re-arrange chartData
		orderedRowIdentifiers = new ArrayList<>();
		getLeafOrder (rowCluster, orderedRowIdentifiers);
		orderedColumnIdentifiers = new ArrayList<>();
		getLeafOrder (columnCluster, orderedColumnIdentifiers);
		
		HeatMapData[][] byRow = orderChartData (chartData, orderedRowIdentifiers, true);
		ordered = orderChartData (byRow, orderedColumnIdentifiers, false);
		
		Collections.reverse(orderedRowIdentifiers);   // need to do this to start placing y axis values from top to bottom
		
		configureHeatMap(chartData, orderedRowIdentifiers, orderedColumnIdentifiers, rowCluster, columnCluster);
		lws.setContents(heatMap);
	}
	
	private HeatMapData[][] orderChartData(HeatMapData[][] chartData, List<String> orderedIdentifiers, boolean byRow) {
		if (chartData.length == 0) {
			return chartData;
		}
		HeatMapData[][] ordered = new HeatMapData[chartData.length][chartData[0].length];
		if (byRow) {
			int i=0;
			for (String rowId : orderedIdentifiers) {
				for (HeatMapData[] row : chartData) {
					if (row[0].getRowIdentifier().equals(rowId)) {
						ordered[i++] = row;
						break;
					}
				}
			}
		} else {
			int i=0;
			for (HeatMapData[] row : chartData) {
				HeatMapData[] orderedRow = new HeatMapData[row.length];
				int j=0;
				for (String columnId : orderedIdentifiers) {
					for (HeatMapData cell : row) {
						if (cell.getColumnIdentifier().equals(columnId)) {
							orderedRow[j++] = cell;
							break;
						}
					}
				}
				ordered[i++] = orderedRow;
			}
		}
		
		return ordered;
	}
	
	public void configureHeatMap (HeatMapData[][] chartData, List<String> orderedRowIdentifiers, List<String> orderedColumnIdentifiers, Cluster rowCluster, Cluster columnCluster) {
		//Configure
		max = findMax(chartData);
		heatMap.setMax(max);
		min = findMin (chartData);
		heatMap.setMin(min);
		heatMap.setDataHeight(dataHeight);
		heatMap.setDataWidth(dataWidth);
		heatMap.getXAxis().setRange(0,  dataWidth);
		heatMap.getYAxis().setRange(0, dataHeight);
		heatMap.getXAxis().setVisible(xAxisVisible);
		heatMap.getYAxis().setVisible(yAxisVisible);
		
		AxisDataSet labelData = new AxisDataSet();
		labelData.setValues(orderedRowIdentifiers);
		((GritsAxis)heatMap.getYAxis()).setLabelData(labelData);
		
		labelData = new AxisDataSet();
		labelData.setValues(orderedColumnIdentifiers);
		((GritsAxis)heatMap.getXAxis()).setLabelData(labelData);
		
		if (xAxisVisible && xAxisOnTop) {
			heatMap.getXAxis().setPrimarySide(false);
			heatMap.setxAxisOnTop(true);
			if (columnDendrogramVisible)
				heatMap.getColumnDendrogram().setPrimarySide(false);  // put it at the bottom
		}
		
		if (yAxisVisible && !yAxisOnRight) {
			heatMap.setyAxisOnRight(yAxisOnRight);
			if (rowDendrogramVisible)
				heatMap.getRowDendrogram().setPrimarySide(false); // put it on the right
		}
		
		if (yAxisVisible && yAxisOnRight) {
			heatMap.getYAxis().setPrimarySide(false);
			heatMap.setyAxisOnRight(yAxisOnRight);
		}
		
		heatMap.getRowDendrogram().setVisible(rowDendrogramVisible);
		heatMap.getColumnDendrogram().setVisible(columnDendrogramVisible);
		heatMap.getRowDendrogram().setModel(rowCluster);
		heatMap.getColumnDendrogram().setModel(columnCluster);
		
		heatMap.setColorMap(colorMap);
		
		heatMap.addROI("ROI 1",	new IROIListener() {
			
			public void roiUpdated(int xIndex, int yIndex, int width, int height) {
				System.out.println("Region of Interest: (" + xIndex + ", " + yIndex 
						+", " + width +", " + height +")");
			}
		}, null);
	}

	private static void getLeafOrder(Cluster cluster, List<String> orderedIdentifiers) {
		if (cluster.isLeaf()) orderedIdentifiers.add (cluster.getName());
        for (Cluster child : cluster.getChildren()) {
            getLeafOrder (child, orderedIdentifiers);
        }
	}

	private static double findMin(HeatMapData[][] chartData) {
		double min = 0.0;
		for (int i=0; i < chartData.length; i++) {
			for (int j = 0; j < chartData[i].length; j++) {
				if (chartData[i][j].getValue() < min) {
					min = chartData[i][j].getValue();
				}
			}
		}
		
		return min;
	}
	
	private static double findMax(HeatMapData[][] chartData) {
		double max = 0.0;
		for (int i=0; i < chartData.length; i++) {
			for (int j = 0; j < chartData[i].length; j++) {
				if (chartData[i][j].getValue() > max) {
					max = chartData[i][j].getValue();
				}
			}
		}
		return max;
	}

	Cluster applyClustering (double[][] distances, String[] identifiers) {
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.performClustering(distances, identifiers, new CompleteLinkageStrategy());
        cluster.toConsole(5);
        return cluster;
	}
	
	public double[][] calculateDistancesForRows (HeatMapData[][] values) {
		double [][] distancesForRows = new double[values.length][values.length];  // for row dendrogram
        for (int i = 0; i < values.length; i++) {
			for (int j = i; j < values.length; j++) {
				if (i == j) {
					distancesForRows[i][j] = 0;
				}
				else {
					distancesForRows[i][j] = calculateDistance (values[i], values[j], distanceFormula, findMin(values), findMax(values));
				}
			}
		}
        
        return distancesForRows;
	}
	
	public double[][] calculateDistancesForColumns (HeatMapData[][] values) {
		double[][] distancesForColumns = new double[values[0].length][values[0].length];  // for column dendrogram
        HeatMapData[][] columnArray = new HeatMapData[values[0].length][values.length];
        //create column vectors and calculate distances
        for (int i=0; i < values[0].length; i++) {
        	for (int j=0; j < values.length; j++) {
        		columnArray[i][j] = values[j][i];
        	}
        }
        	
        for (int i=0; i < columnArray.length; i++) {
        	for (int j=i; j < columnArray.length; j++) {
		        if (i == j) {
		        	distancesForColumns[i][j] = 0;
				}
				else {
					distancesForColumns[i][j] = calculateDistance (columnArray[i], columnArray[j], distanceFormula, findMin(values), findMax(values));
				}
        	}
        }
        
        return distancesForColumns;
	}
	
	/**
	 * calculates euclidean distance between two vectors
	 * 
	 * @param array1
	 * @param array2
	 * @param max2 
	 * @param min2 
	 * @return the distance
	 */
	public static double calculateDistance(HeatMapData[] array1, HeatMapData[] array2, DistanceFormula formula, Double min, Double max )
    {
		if (array1.length != array2.length) {
			throw new RuntimeException("Size of the data do not match");
		}
		if (formula.equals(DistanceFormula.Spherical)) {
			return sphericalDistance(array1, array2, min, max);
		} else {
			return euclideanDistance(array1, array2);
		}
    }
	
	public static double euclideanDistance(HeatMapData[] array1, HeatMapData[] array2) {
		double Sum = 0.0;
        for(int i=0;i<array1.length;i++) {
        	Sum = Sum + Math.pow((array1[i].getValue()-array2[i].getValue()),2.0);
        }
        return Math.sqrt(Sum);
	}
	
	public static double sphericalDistance (HeatMapData[] array1, HeatMapData[] array2, Double min, Double max) {
		// need to normalize values to [0-1] first
		HeatMapData[] normalized1 = normalize(array1, min, max);
		HeatMapData[] normalized2 = normalize(array2, min, max);
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < normalized1.length; i++) {	
	        dotProduct += normalized1[i].getValue() * normalized2[i].getValue();
	        normA += Math.pow(normalized1[i].getValue(), 2);
	        normB += Math.pow(normalized2[i].getValue(), 2);
	    } 
	    
	    return Math.acos(dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
	}
	
	public static HeatMapData[] normalize (HeatMapData[] array, double min, double max) {
		HeatMapData[] normalized = new HeatMapData[array.length];
		int i=0;
		for (HeatMapData heatMapData : array) {
			HeatMapData norm = new HeatMapData();
			norm.setColumnIdentifier(heatMapData.getColumnIdentifier());
			norm.setRowIdentifier(heatMapData.getRowIdentifier());
			norm.setValue(heatMapData.getValue() / (max-min));
			normalized[i++] = norm;
		}
		
		return normalized;
	}

	public void setDataArray() {
		//Create one dimensional data from values
		final double[] simuData = new double[dataWidth * dataHeight * 2];
		final double[] data = new double[dataWidth * dataHeight];
		for (int i = 0; i < dataHeight; i++) {
			for (int j = 0; j < dataWidth; j++) {
				simuData[i * dataWidth + j] = (double) ordered[i][j].getValue();
			}
		}
		
		if (future != null && !future.isCancelled())
			future.cancel(true);
		if (scheduler != null && !scheduler.isShutdown())
			scheduler.shutdown();
		
		// Update the graph in another thread.
		scheduler = Executors.newScheduledThreadPool(1);
		future = scheduler.scheduleAtFixedRate(
				new Runnable() {

					public void run() {
						System.arraycopy(simuData, count % dataWidth, data, 0,
								dataWidth * dataHeight);

						Display.getDefault().asyncExec(new Runnable() {

							public void run() {
								count++;
								heatMap.setDataArray(simuData);
							}
						});
					}
				}, 100, 10, TimeUnit.MILLISECONDS);
	}
	
	public void finish() {
		future.cancel(true);
		scheduler.shutdown();
	}
	
	

	public boolean isRowDendrogramVisible() {
		return rowDendrogramVisible;
	}

	public void setRowDendrogramVisible(boolean rowDendrogramVisible) {
		this.rowDendrogramVisible = rowDendrogramVisible;
	}

	public boolean isColumnDendrogramVisible() {
		return columnDendrogramVisible;
	}

	public void setColumnDendrogramVisible(boolean columnDendrogramVisible) {
		this.columnDendrogramVisible = columnDendrogramVisible;
	}

	public boolean isxAxisVisible() {
		return xAxisVisible;
	}

	public void setxAxisVisible(boolean xAxisVisible) {
		this.xAxisVisible = xAxisVisible;
	}

	public boolean isyAxisVisible() {
		return yAxisVisible;
	}

	public void setyAxisVisible(boolean yAxisVisible) {
		this.yAxisVisible = yAxisVisible;
	}

	public boolean isxAxisOnTop() {
		return xAxisOnTop;
	}

	public void setxAxisOnTop(boolean xAxisOnTop) {
		this.xAxisOnTop = xAxisOnTop;
	}

	public boolean isyAxisOnRight() {
		return yAxisOnRight;
	}

	public void setyAxisOnRight(boolean yAxisOnRight) {
		this.yAxisOnRight = yAxisOnRight;
	}

	public ColorMap getColorMap() {
		return colorMap;
	}

	public void setColorMap(ColorMap colorMap) {
		this.colorMap = colorMap;
	}

	public void recalculateClusters(double column1, double column2, double row1, double row2) throws Exception {
		int rIndex1 = (int)Math.floor(row1);
		int rIndex2 = (int)Math.floor(row2);
		int cIndex1 = (int)Math.floor(column1);
		int cIndex2 = (int)Math.floor(column2);
		
		// extract the data from "ordered" with the given range
		// extract row and column identifiers
		HeatMapData[][] cropped = new HeatMapData[rIndex1-rIndex2+1][cIndex2-cIndex1+1];
		List<String> croppedRowIdentifiers = new ArrayList<>();
		List<String> croppedColumnIdentifiers = new ArrayList<>();
		int k=0;
		int lastIndex = ordered.length-1;
		for (int i=rIndex1; i >= rIndex2; i--) {
			if (!croppedRowIdentifiers.contains(orderedRowIdentifiers.get(i)))
				croppedRowIdentifiers.add(orderedRowIdentifiers.get(i));
			int m=0;
			for (int j=cIndex1; j <= cIndex2; j++) {
				if (!croppedColumnIdentifiers.contains(orderedColumnIdentifiers.get(j)))
					croppedColumnIdentifiers.add(orderedColumnIdentifiers.get(j));
				cropped[k][m++] = ordered[lastIndex-i][j];
			}
			k++;
		}
		
		dataWidth = croppedColumnIdentifiers.size();
		dataHeight = croppedRowIdentifiers.size();
		
		// reverse rowIdentifiers to the original order
	//	Collections.reverse(croppedRowIdentifiers);
		// get the original crop and axes ranges
		Rectangle originalCrop = heatMap.getOriginalCrop();
		Range xAxisRange = heatMap.getxAxisRange();
		Range yAxisRange = heatMap.getyAxisRange();
		heatMap = new GritsHeatMapFigure(this);
		heatMap.setOriginalCrop(originalCrop);
		heatMap.setxAxisRange(xAxisRange);
		heatMap.setyAxisRange(yAxisRange);
		cluster(cropped, croppedColumnIdentifiers.toArray(new String[croppedColumnIdentifiers.size()]), croppedRowIdentifiers.toArray(new String[croppedRowIdentifiers.size()]));
		
	}

	public void recalculateWithOriginalData() {
		dataWidth = originaldataWidth;
		dataHeight = originaldataHeight;
		Rectangle originalCrop = heatMap.getOriginalCrop();
		Range xAxisRange = heatMap.getxAxisRange();
		Range yAxisRange = heatMap.getyAxisRange();
		heatMap = new GritsHeatMapFigure(this);
		heatMap.setOriginalCrop(originalCrop);
		heatMap.setxAxisRange(xAxisRange);
		heatMap.setyAxisRange(yAxisRange);
		cluster (chartData, columnIdentifiers, rowIdentifiers);		
	}
	
	
	public static void main(String[] args) {
		final Shell shell = new Shell();
		shell.setSize(500, 450);
		shell.setLayout(new FillLayout());
		shell.open();

	//	final double[][] values = {{-100,100, 100, -100}, {34,-65, -50, 32}, {97,-12, -43, 56}, {-39,67, 65, 75},  {12,-10, 10, -30}, {86,32, 65, 45}, {80,32, 64, -5}, {45,43, 97, 20}, {66,54, 54, 3}, {-72, 19, 14, 64}};
		
	//	String[] rowNames = new String[] { "O1", "O2", "O3", "O4", "O5", "O6", "07", "08", "09", "10" };
	//	String[] columnNames = new String[] { "O1", "O2", "O3", "O4"};
		
		
		
		List<String> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		HeatMapData[][] chartData = readCSVFile(rows, columns);
		
		//HeatMapData[][] chartData = new HeatMapData[values.length][values[0].length];
	/*	int i=0;
		for (double[] row : values) {
			int j = 0;
			for (double d : row) {
				HeatMapData data = new HeatMapData();
				data.setValue(d);
				data.setRowIdentifier(rowNames[i]);
				data.setColumnIdentifier(columnNames[j]);
				chartData[i][j] = data;
				j++;
			}
			i++;
		}*/
		HeatMapChart chart = new HeatMapChart(shell);
		chart.setBounds (shell.getClientArea());
		try {
			chart.initializeChart(chartData, columns.toArray(new String[columns.size()]), rows.toArray(new String[rows.size()]));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		chart.setDataArray();

		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		chart.finish();
	}

	private static HeatMapData[][] readCSVFile(List<String> rowNames, List<String> columnNames) {
		String fileToParse = "/Users/sena/Desktop/heatmap75/tiemeyercluster4b.csv";
        BufferedReader fileReader = null;
  
        //Delimiter used in CSV file
        final String DELIMITER = ",";
        try
        {
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileToParse));
            List<List<HeatMapData>> data = new ArrayList<>();
            int rowIndex=0;
            
            //Read the file line by line
            while ((line = fileReader.readLine()) != null) 
            {
            	List<HeatMapData> row = new ArrayList<>();
                //Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                if (tokens.length == 0) {
                	continue;
                }
                int columnIndex=0;
                for(String token : tokens)
                {
                	if (rowIndex == 0 || columnIndex == 0) {
	                    if (rowIndex == 0) { // first row
	                    	if (columnIndex == 0) {
	                    		columnIndex ++;
	                    		continue;
	                    	}
	                    	if (!token.isEmpty())
	                    		columnNames.add(token);
	                    }
	                    
	                    if (columnIndex == 0) {
	                    	if (!token.isEmpty())
	                    		rowNames.add(token);
	                    }
                	}
                    else {
                    	HeatMapData d = new HeatMapData();
                    	d.setValue(Double.parseDouble(token));
                    	row.add(d);
                    	d.setColumnIdentifier(columnNames.get(columnIndex-1));
                    	d.setRowIdentifier(rowNames.get(rowIndex-1));
                    	
                    }
                	columnIndex ++;
                }
                if (rowIndex != 0) {
                	if (row.size() > 0)
                		data.add(row);
                }
                rowIndex ++;
            }
            
            HeatMapData[][] chartData = new HeatMapData[data.size()][data.get(0).size()];
            int i=0;
            for (List<HeatMapData> row : data) {
            	int j=0;
				for (HeatMapData heatMapData : row) {
					chartData[i][j++] = heatMapData;
				}
				i++;
			}

            fileReader.close();
            return chartData;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
	}
	
}
