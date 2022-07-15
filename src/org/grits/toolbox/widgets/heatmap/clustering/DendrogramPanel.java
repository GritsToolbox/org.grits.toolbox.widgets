/*******************************************************************************
 * Copyright 2013 Lars Behnke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.grits.toolbox.widgets.heatmap.clustering;

import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.apporiented.algorithm.clustering.Cluster;

/**
 * 
 * @modifiedBy sena
 * make it work with draw2D/swt instead of awt
 *
 */
public class DendrogramPanel extends Figure {

    //final static BasicStroke solidStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

    private Cluster model;
    private ClusterComponent component;
    private Color lineColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    private boolean showDistanceValues = false;
    private boolean showScale = false;
    private int margin = 10;
    private int borderTop = 2;
    private int borderLeft = 2;
    private int borderRight = 2;
    private int borderBottom = 2;
    private int scalePadding = 10;
    private int scaleTickLength = 4;
   // private int scaleTickLabelPadding = 4;
    private double scaleValueInterval = 0;
    private int scaleValueDecimals = 0;

    private double xModelOrigin = 0.0;
    private double yModelOrigin = 0.0;
    private double wModel = 0.0;
    private double hModel = 0.0;
    
    boolean horizontal = false;
    boolean primarySide = true; // primary side for horizontal one is the top, primary side for the vertical one is left

	private Font labelFont;
    
    public DendrogramPanel() {
    	Font sysFont = Display.getCurrent().getSystemFont();
		labelFont = XYGraphMediaFactory.getInstance().getFont(
				new FontData(sysFont.getFontData()[0].getName(), 8, SWT.NORMAL)); //$NON-NLS-1$
	}
    
    public boolean isHorizontal() {
		return horizontal;
	}
    
    public void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
	}

    public void setPrimarySide(boolean primarySide) {
		this.primarySide = primarySide;
		// recreate component if already created
		if (component != null && model != null) {
			component = createComponent(model);
			updateModelMetrics();
		}
	}
    
    public boolean isPrimarySide() {
		return primarySide;
	}
    
    public boolean isShowDistanceValues() {
        return showDistanceValues;
    }

    public void setShowDistances(boolean showDistanceValues) {
        this.showDistanceValues = showDistanceValues;
    }

    public boolean isShowScale() {
        return showScale;
    }

    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    public int getScalePadding() {
        return scalePadding;
    }

    public void setScalePadding(int scalePadding) {
        this.scalePadding = scalePadding;
    }

    public int getScaleTickLength() {
        return scaleTickLength;
    }

    public void setScaleTickLength(int scaleTickLength) {
        this.scaleTickLength = scaleTickLength;
    }

    public double getScaleValueInterval() {
        return scaleValueInterval;
    }

    public void setScaleValueInterval(double scaleTickInterval) {
        this.scaleValueInterval = scaleTickInterval;
    }

    public int getScaleValueDecimals() {
        return scaleValueDecimals;
    }

    public void setScaleValueDecimals(int scaleValueDecimals) {
        this.scaleValueDecimals = scaleValueDecimals;
    }

    public int getBorderTop() {
        return borderTop;
    }

    public void setBorderTop(int borderTop) {
        this.borderTop = borderTop;
    }

    public int getBorderLeft() {
        return borderLeft;
    }

    public void setBorderLeft(int borderLeft) {
        this.borderLeft = borderLeft;
    }

    public int getBorderRight() {
        return borderRight;
    }

    public void setBorderRight(int borderRight) {
        this.borderRight = borderRight;
    }

    public int getBorderBottom() {
        return borderBottom;
    }

    public void setBorderBottom(int borderBottom) {
        this.borderBottom = borderBottom;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public Cluster getModel() {
        return model;
    }

    public void setModel(Cluster model) {
        this.model = model;
        component = createComponent(model);
        updateModelMetrics();
    }

    private void updateModelMetrics() {
        double minX = component.getRectMinX();
        double maxX = component.getRectMaxX();
        double minY = component.getRectMinY();
        double maxY = component.getRectMaxY();

        xModelOrigin = minX;
        yModelOrigin = minY;
        wModel = maxX - minX;
        hModel = maxY - minY;
    }

    /**
     * 
     * @param cluster
     * @param initCoord
     * @param clusterSize is the height if the dendrogram is vertical, width if horizontal
     * @return
     */
    private ClusterComponent createComponent(Cluster cluster, VCoord initCoord, double clusterHeight, double clusterWidth) {

        ClusterComponent comp = null;
        if (cluster != null) {
            comp = new ClusterComponent(cluster, cluster.isLeaf(), initCoord);
            
            if (!horizontal) {
            	if (primarySide) {
		            double leafHeight = clusterHeight / cluster.countLeafs();
		            double yChild = initCoord.getY() - (clusterHeight / 2);
		            double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		            for (Cluster child : cluster.getChildren()) {
		                int childLeafCount = child.countLeafs();
		                double childHeight = childLeafCount * leafHeight;
		                double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
		                VCoord childInitCoord = new VCoord(initCoord.getX() + (distance - childDistance), yChild + childHeight
		                        / 2.0);
		                yChild += childHeight;
		
		                /* Traverse cluster node tree */
		                ClusterComponent childComp = createComponent(child, childInitCoord, childHeight, clusterWidth);
		
		                childComp.setLinkPoint(initCoord);
		                comp.getChildren().add(childComp);
		            } 
            	} else {
            		double leafHeight = clusterHeight / cluster.countLeafs();
		            double yChild = initCoord.getY() - (clusterHeight / 2);
		            double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		            for (Cluster child : cluster.getChildren()) {
		                int childLeafCount = child.countLeafs();
		                double childHeight = childLeafCount * leafHeight;
		                double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
		                VCoord childInitCoord = new VCoord(initCoord.getX() - (distance - childDistance), yChild + childHeight
		                        / 2.0);
		                yChild += childHeight;
		
		                /* Traverse cluster node tree */
		                ClusterComponent childComp = createComponent(child, childInitCoord, childHeight, clusterWidth);
		
		                childComp.setLinkPoint(initCoord);
		                comp.getChildren().add(childComp);
		            }
            	}
            } else {  // horizontal
            	if (primarySide) {
	            	double leafWidth = clusterWidth / cluster.countLeafs();
	            	double xChild = initCoord.getX() - (clusterWidth /2);
	            	double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		            for (Cluster child : cluster.getChildren()) {
		            	int childLeafCount = child.countLeafs();
		            	double childWidth = childLeafCount * leafWidth;
		            	double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
		                VCoord childInitCoord = new VCoord(xChild + childWidth / 2.0, initCoord.getY() + (distance - childDistance));
		                xChild += childWidth;
		                
		                /* Traverse cluster node tree */
		                ClusterComponent childComp = createComponent(child, childInitCoord, clusterHeight, childWidth);
		
		                childComp.setLinkPoint(initCoord);
		                comp.getChildren().add(childComp);
		            }
            	} else {
            		double leafWidth = clusterWidth / cluster.countLeafs();
	            	double xChild = initCoord.getX() - (clusterWidth /2);
	            	double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		            for (Cluster child : cluster.getChildren()) {
		            	int childLeafCount = child.countLeafs();
		            	double childWidth = childLeafCount * leafWidth;
		            	double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
		                VCoord childInitCoord = new VCoord(xChild + childWidth / 2.0, initCoord.getY() - (distance - childDistance));
		                xChild += childWidth;
		                
		                /* Traverse cluster node tree */
		                ClusterComponent childComp = createComponent(child, childInitCoord, clusterHeight, childWidth);
		
		                childComp.setLinkPoint(initCoord);
		                comp.getChildren().add(childComp);
		            }
            	}
            }
        }
        return comp;

    }

    private ClusterComponent createComponent(Cluster model) {

    	if (!horizontal) {
    		VCoord initCoord = null;
    		double virtualModelHeight = 1;
    		double virtualModelWidth = 1;
    		if (primarySide) {
    			initCoord = new VCoord(0, virtualModelHeight / 2);
    		} else {
    			initCoord = new VCoord(1, virtualModelHeight / 2);
    		}
	        ClusterComponent comp = createComponent(model, initCoord, virtualModelWidth, virtualModelHeight);
	        comp.setLinkPoint(initCoord);
	        return comp;
	        
    	} else {
    		double virtualModelWidth = 1;
    		double virtualModelHeight = 1;
    		VCoord initCoord = null;
    		if (primarySide)
    			initCoord = new VCoord (0, virtualModelWidth / 2);
    		else 
    			initCoord = new VCoord (virtualModelHeight, virtualModelWidth / 2);
    		ClusterComponent comp = createComponent(model, initCoord, virtualModelWidth, virtualModelHeight);
	        comp.setLinkPoint(initCoord);
	        return comp;
    	}
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics g2 = g;
        g2.setAntialias(SWT.ON);
        g2.setForegroundColor(lineColor);
      //  g2.setColor(lineColor);
        g2.setLineCap(SWT.CAP_SQUARE);
        g2.setLineJoin(SWT.JOIN_ROUND);
      // g2.setStroke(solidStroke);

        int wDisplay = getWidth() - borderLeft - borderRight;
        int hDisplay = getHeight() - borderTop - borderBottom;
        //int xDisplayOrigin = borderLeft;
        //int yDisplayOrigin = borderBottom;
        int xDisplayOrigin = getClientArea().x + borderLeft;
        int yDisplayOrigin = getClientArea().y + borderBottom;

        if (component != null) {
            //int nameGutterWidth = component.getMaxNameWidth(g2, false) + component.getNamePadding();
        	int nameGutterWidth = 0;
            wDisplay -= nameGutterWidth;

            /* Calculate conversion factor and offset for display */
            double xFactor = wDisplay / wModel;
            double yFactor = hDisplay / hModel;
            int xOffset = (int) (xDisplayOrigin - xModelOrigin * xFactor);
            int yOffset = (int) (yDisplayOrigin - yModelOrigin * yFactor);
            component.paint(g2, xOffset, yOffset, xFactor, yFactor, showDistanceValues, horizontal, primarySide);
        } else {

            /* No data available */
            String str = "No data";
            //Rectangle rect = g2.getFontMetrics().getStringBounds(str, g2);
            Dimension rect = FigureUtilities.getTextExtents(str, labelFont);
            int xt = (int) (wDisplay / 2.0 - rect.width / 2.0);
            int yt = (int) (hDisplay / 2.0 - rect.height / 2.0);
            g2.drawString(str, xt, yt);
        }
    }
    
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
    	Dimension size = new Dimension(wHint, hHint);
		if (isVisible()) {
			if (isHorizontal()) {
				borderLeft = (wHint / countLevels(model)) / 2;
				borderRight = borderLeft;
				int minHeight = model.countLeafs() * margin;
				double factor = hHint/minHeight;
				if (factor > 10)
					margin += factor * 0.3;
				size.height = model.countLeafs() * margin;
			} else {
				borderBottom = (hHint / model.countLeafs()) / 2;
				borderTop = borderBottom;
				int minWidth = countLevels(model) * margin;
				double factor = wHint/minWidth;
				if (factor > 10)
					margin += factor * 0.3;
				size.width = countLevels(model) * margin;
			}
		} else { // Not visible, flatten it to use zero height resp. width
			if (isHorizontal())
				size.height = 0;
			else
				size.width = 0;
		}
		return size;

	}

    private int getHeight() {
    	return getClientArea().height;
	}

	private int getWidth() {
		return getClientArea().width;
	}
	
	private static int countLevels(Cluster node)
    {
		if (node == null) // model is not set - no data
			return 0;
        List<Cluster> children = node.getChildren();
        if (children.size() == 0)
        {
            return 1;
        }
        Cluster child0 = children.get(0);
        Cluster child1 = children.get(1);
        return 1+Math.max(countLevels(child0), countLevels(child1));
    }
	
	public void recalculate(double t1, double t2) {
		// recreate the component
		component = createComponent(model);
        updateModelMetrics();
		revalidate();
		repaint();
	}

/*	public static void main(String[] args) {
    	
    	Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Example");
        shell.setSize(400, 300);
        shell.setLayout(new FillLayout());
        
        DendrogramPanel figure = new DendrogramPanel();
        LightweightSystem lws = new LightweightSystem(shell);
        
        Cluster cluster = createSampleCluster();
        figure.setModel(cluster);
        
        lws.setContents(figure);
    
        // Enter event loop
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    public static Cluster createSampleCluster() {
        double[][] distances = new double[][] { { 0, 1, 9, 7, 11, 14 }, { 1, 0, 4, 3, 8, 10 }, { 9, 4, 0, 9, 2, 8 },
                { 7, 3, 9, 0, 6, 13 }, { 11, 8, 2, 6, 0, 10 }, { 14, 10, 8, 13, 10, 0 } };
        String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.performClustering(distances, names, new AverageLinkageStrategy());
        cluster.toConsole(0);
        return cluster;
    }*/

	
}
