package org.grits.toolbox.widgets.processDialog;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.widgets.progress.IProgressListener;

public class ProgressBarListener implements IProgressListener {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(ProgressBarListener.class);
	protected int iMax = 0;
	private int iCurValue = -1;
	protected String sCurText = null;
	protected TextProgressBar curProgressBar = null;	
	protected TextProgressBar progressBarIndeterminant = null;	
	protected TextProgressBar progressBarDeterminant = null;
	protected Composite minorContainer = null;
	protected StackLayout stackLayout = null;
	protected Display display = null;
	protected Shell shell = null;
	
	// default constructor to have a single determinant progress bar
	public ProgressBarListener(TextProgressBar progressBar, Display display) {
		this.curProgressBar = progressBar;
		this.display = display;
	}

	// option to have the ability to switch between a determinant and indeterminant progress bar
	public ProgressBarListener(TextProgressBar progressBarDeterminant, TextProgressBar progressBarIndeterminant, ProgressType progressType, 
								StackLayout stackLayout, Display display, Shell shell) {
		this.progressBarDeterminant = progressBarDeterminant;
		this.progressBarIndeterminant = progressBarIndeterminant;
		this.display = display;
		this.shell = shell;
		this.stackLayout = stackLayout;
		setProgressType(progressType);
	}
	
	@Override
	public void setProgressMessage(final String _sMessage) {
		if( display.isDisposed() )
			return;
		try {
			display.syncExec(new Runnable() 
			{
				public void run() {
					if( curProgressBar.isDisposed() ) 
						return;
					curProgressBar.setText("");
					curProgressBar.update();
					curProgressBar.setText(_sMessage);
					sCurText = _sMessage;
					curProgressBar.update();
				}
			});	
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}

	}

	@Override
	public void setProgressValue( final int _iValue) {
		if( display.isDisposed() )
			return;
		try {
			display.syncExec(new Runnable() {
				public void run() {
					if( curProgressBar.isDisposed() ) 
						return;
					int iCount = (int) (((double) _iValue / iMax ) * 100.0);	
					curProgressBar.setSelection(iCount - 1);
					curProgressBar.update();
					curProgressBar.setSelection(iCount);
					curProgressBar.update();
					iCurValue = _iValue;
				}
			});
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void setMaxValue(final int _iValue) {
		if( display.isDisposed() )
			return;
		try {
			iMax = _iValue;
			display.syncExec(new Runnable() {
				public void run() {
					if( curProgressBar.isDisposed() ) 
						return;
					curProgressBar.setSelection(1);
					curProgressBar.setSelection(0);
					//				int iCount = (int) (((double) _iValue / iMax ) * 100.0);
					//				progressBar.setMaximum(_iValue);
					curProgressBar.setMaximum(100);
					iCurValue = 0;
				}
			});
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void setMinValue(final int _iValue) {
		if( display.isDisposed() )
			return;
		try {
			display.syncExec(new Runnable() {
				public void run() {
					if( curProgressBar.isDisposed() ) 
						return;
					curProgressBar.setSelection(0);
					iCurValue = 0;
					curProgressBar.setMinimum(_iValue);
				}
			});
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	protected void setDeterminant( final TextProgressBar pbD, final TextProgressBar pbI, final StackLayout sl ) {
		this.display.syncExec(new Runnable() {
			public void run() 
			{
				pbI.setVisible(false);
				sl.topControl = pbD;
				pbD.setVisible(true);
				shell.layout();
			}
		});			
	}

	protected void setIndeterminant( final TextProgressBar pbD, final TextProgressBar pbI, final StackLayout sl  ) {
		this.display.syncExec(new Runnable() {
			public void run() 
			{
				pbD.setVisible(false);
				sl.topControl = pbI;
				pbI.setVisible(true);				
				shell.layout();
				shell.pack();
			}
		});			
	}
	
	public int getCurValue() {
		return iCurValue;
	}
	
	public String getCurText() {
		return sCurText;
	}

	@Override
	public void setError(String _sMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setError(String _sMessage, Throwable _t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgressType(ProgressType progressType) {
		if( progressType == ProgressType.Determinant ) {
			setDeterminant(this.progressBarDeterminant, this.progressBarIndeterminant, this.stackLayout);
			this.curProgressBar = this.progressBarDeterminant;
		} else {
			setIndeterminant(this.progressBarDeterminant, this.progressBarIndeterminant, this.stackLayout);		
			this.curProgressBar = this.progressBarIndeterminant;
		}
	}
	
}
