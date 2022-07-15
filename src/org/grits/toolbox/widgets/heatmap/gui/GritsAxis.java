package org.grits.toolbox.widgets.heatmap.gui;

import java.util.Arrays;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.widgets.heatmap.AxisDataSet;

/**
 * The Grits axis figure.
 * 
 * It puts string tick labels and no tick marks
 * cannot use logScale since it does not make sense with a non-numeric tick values
 * 
 */
public class GritsAxis extends Axis {
	
	private static final int SPACE_BTW_MARK_LABEL = 2;
	GritsTickLabels tickLabels;
	private int margin;
	private int length;
//	private GritsTickMarks tickMarks;

	public GritsAxis(String title, boolean yAxis) {
		super(title, yAxis);
		// remove previously added tickLabels and tickMarks
		this.removeAll();
		tickLabels = new GritsTickLabels(this);
	//	tickMarks = new GritsTickMarks(this);
	//	add(tickMarks);
		add(tickLabels);
		Font sysFont = Display.getCurrent().getSystemFont();
		setFont(XYGraphMediaFactory.getInstance().getFont(new FontData(sysFont.getFontData()[0].getName(), 11, SWT.NORMAL)));
	}
	
	@Override
	protected void layout() {
	//	super.layout();
		updateTick();
		Rectangle area = getClientArea();
		Dimension newSize = adjustForRotatedTickLabels(area.getSize());
		if (newSize.height > area.height) {
			area.setSize(adjustForRotatedTickLabels(area.getSize()));
			// need to adjust parent layout
			((GritsHeatMapFigure)getParent()).layout();
		}
		if (isHorizontal() && getTickLabelSide() == LabelSide.Primary) {
			tickLabels.setBounds(new Rectangle(area.x, area.y + 
					+ SPACE_BTW_MARK_LABEL, area.width, area.height));
		//	tickMarks.setBounds(area);
		} else if (isHorizontal() && getTickLabelSide() == LabelSide.Secondary) {
			tickLabels.setBounds(new Rectangle(area.x, area.y + area.height 
					- tickLabels.getTickLabelMaxHeight() - SPACE_BTW_MARK_LABEL, area.width, tickLabels.getTickLabelMaxHeight()));
		//	tickMarks.setBounds(new Rectangle(area.x, area.y + area.height - LinearScaleTickMarks.MAJOR_TICK_LENGTH,
		//			area.width, LinearScaleTickMarks.MAJOR_TICK_LENGTH));
		} else if (getTickLabelSide() == LabelSide.Primary) {
			tickLabels.setBounds(new Rectangle(area.x + area.width 
					- tickLabels.getTickLabelMaxLength() - SPACE_BTW_MARK_LABEL, area.y, tickLabels
					.getTickLabelMaxLength(), area.height));
		//	tickMarks.setBounds(new Rectangle(area.x + area.width - LinearScaleTickMarks.MAJOR_TICK_LENGTH, area.y,
		//			LinearScaleTickMarks.MAJOR_TICK_LENGTH, area.height));
		} else {
			tickLabels.setBounds(new Rectangle(area.x + SPACE_BTW_MARK_LABEL,
					area.y, tickLabels.getTickLabelMaxLength(), area.height));
		//	tickMarks.setBounds(new Rectangle(area.x, area.y, LinearScaleTickMarks.MAJOR_TICK_LENGTH, area.height));
		}
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {

		Dimension size = new Dimension(wHint, hHint);
		GritsTickLabels fakeTickLabels = new GritsTickLabels(this);
		AxisDataSet dataSet = new AxisDataSet();
		dataSet.setValues(Arrays.asList("Label112"));
		fakeTickLabels.setLabelData(dataSet);

		if (isHorizontal()) {
			// length = wHint;
			fakeTickLabels.update(wHint - 2 * getMargin());
			size.height = (int) fakeTickLabels.getTickLabelMaxHeight() + SPACE_BTW_MARK_LABEL;	
		} else {
			// length = hHint;
			fakeTickLabels.update(hHint - 2 * getMargin());
			size.width = (int) fakeTickLabels.getTickLabelMaxLength() + SPACE_BTW_MARK_LABEL;		
		}
		
		if (isVisible()) {
			if (isHorizontal())
				size.height += FigureUtilities.getTextExtents(getTitle(), getTitleFont()).height;
			else
				size.width += FigureUtilities.getTextExtents(getTitle(), getTitleFont()).height;
		} else { // Not visible, flatten it to use zero height resp. width
			if (isHorizontal())
				size.height = 0;
			else
				size.width = 0;
		}

		return size;

	}
	
	/**
	 * Gets the scale tick labels.
	 * 
	 * @return the scale tick labels
	 */
	public GritsTickLabels getTickLabels() {
		return tickLabels;
	}

	/**
	 * Gets the scale tick marks.
	 * 
	 * @return the scale tick marks
	 */
	/*public GritsTickMarks getTickMarks() {
		return tickMarks;
	}*/

	
	/*
	 * @see IAxisTick#setFont(Font)
	 */
	@Override
	public void setFont(Font font) {
		if (font != null && font.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		tickLabels.setFont(font);
		super.setFont(font);

	}

	/*
	 * @see IAxisTick#setForeground(Color)
	 */
	@Override
	public void setForegroundColor(Color color) {
	//	tickMarks.setForegroundColor(color);
		tickLabels.setForegroundColor(color);
		super.setForegroundColor(color);
	}
	
	/**
	 * need to use our own tick labels
	 */
	@Override
	public void updateTick() {
		if (isDirty()) {
			calcMargin();
			setDirty(false);
			length = isHorizontal() ? getClientArea().width : getClientArea().height;
			if (length > 2 * margin)
				tickLabels.update(length - 2 * margin);

		}
		
	}
	
	/**
	 * @return the length of the whole scale (include margin)
	 */
	public int getLength() {
		return length;
	}

	@Override
	public int getMargin() {
		updateTick();
		return margin;
	}
	
	/**
	 * @return the length of the tick part (without margin)
	 */
	public int getTickLength() {
		return length - 2 * getMargin();
	}
	
	/**
	 * Get the position of the value based on scale.
	 * 
	 * @param value
	 *            the value to find its position. Support value out of range.
	 * @param relative
	 *            return the position relative to the left/bottom bound of the
	 *            scale if true. If false, return the absolute position which
	 *            has the scale bounds counted.
	 * @return position in pixels
	 */
	public int getValuePosition(double value, boolean relative) {
		if (dirty)
			updateTick();
		// coerce to range
		// value = value < min ? min : (value > max ? max : value);
		int pixelsToStart = 0;
		if (isLogScaleEnabled()) {
			if (value <= 0)
				value = min;
			// throw new IllegalArgumentException(
			// "Invalid value: value must be greater than 0");
			pixelsToStart = (int) ((Math.log10(value) - Math.log10(min)) / (Math.log10(max) - Math.log10(min)) * (length - 2 * margin))
					+ margin;
		} else
			pixelsToStart = (int) ((value - min) / (max - min) * (length - 2 * margin)) + margin;

		if (relative) {
			if (getOrientation() == Orientation.HORIZONTAL)
				return pixelsToStart;
			else
				return length - pixelsToStart;
		} else {
			if (getOrientation() == Orientation.HORIZONTAL)
				return pixelsToStart + bounds.x;
			else
				return length - pixelsToStart + bounds.y;
		}
	}

	/**
	 * Get the corresponding value on the position of the scale.
	 * 
	 * @param the
	 *            position.
	 * @param true if the position is relative to the left/bottom bound of the
	 *        scale; False if it is the absolute position.
	 * @return the value corresponding to the position.
	 */
	public double getPositionValue(int position, boolean relative) {
		updateTick();
		// coerce to range
		double min = getRange().getLower();
		double max = getRange().getUpper();
		int pixelsToStart;
		double value;
		if (relative) {
			if (isHorizontal())
				pixelsToStart = position;
			else
				pixelsToStart = length - position;
		} else {
			if (isHorizontal())
				pixelsToStart = position - bounds.x;
			else
				pixelsToStart = length + bounds.y - position;
		}

		if (isLogScaleEnabled())
			value = Math.pow(10, (pixelsToStart - margin) * (Math.log10(max) - Math.log10(min)) / (length - 2 * margin)
					+ Math.log10(min));
		else
			value = (pixelsToStart - margin) * (max - min) / (length - 2 * margin) + min;

		return value;
	}

	
	private void calcMargin() {
		if (isHorizontal()) {
			// find the longest label and calculate margin from its width
			AxisDataSet labels = tickLabels.getLabelData();
			double max = 0;
			for (double i=getRange().getLower(); i < getRange().getUpper(); i++) {
				String label = labels.getLabel(i);
				if (label != null) {
					double width = FigureUtilities.getTextExtents(label, getFont()).width;
					if (width > max)
						max = width;
				}
			}
			margin = (int) Math.ceil(max / 2);
		/*	margin = (int) Math.ceil(Math.max(
					FigureUtilities.getTextExtents(tickLabels.getLabelData().getLabel(0), getFont()).width,
					FigureUtilities.getTextExtents(tickLabels.getLabelData().getLabel(0), getFont()).width) / 2.0);*/
		} else
			margin = (int) Math.ceil(Math.max(
					FigureUtilities.getTextExtents(tickLabels.getLabelData().getLabel(getRange().getLower()), getFont()).height,
					FigureUtilities.getTextExtents(tickLabels.getLabelData().getLabel(getRange().getUpper()), getFont()).height) / 2.0);  
	}
	
	public void setLabelData(AxisDataSet labelData) {
		tickLabels.setLabelData(labelData);
	}

	public Dimension adjustForRotatedTickLabels(Dimension xAxisSize) {
		if (!isHorizontal()) {   // only valid for xAxis
			return xAxisSize;
		}
		double angle = tickLabels.getTickLabelAngle();
        if (angle == 0) {
           return xAxisSize;
        }

        // update tick label height
        int tickLabelMaxLength = getTickLabels().getTickLabelMaxLength();
        int height = getMargin()
                + (int) (tickLabelMaxLength
                        * Math.sin(Math.toRadians(angle)) + FigureUtilities.getTextExtents(tickLabels.getLabelData().getLabel(getRange().getLower()), getFont()).height
                        * Math.cos(Math.toRadians(angle)));
        int delta = height - xAxisSize.height;
        xAxisSize.height += delta;	
        return xAxisSize;
	}
}