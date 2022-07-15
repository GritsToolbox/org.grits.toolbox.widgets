package org.grits.toolbox.widgets.processDialog;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.widgets.progress.CancelableThread;
import org.grits.toolbox.widgets.progress.IProgressHandler;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.tools.GRITSWorker;

/**
 * Progress Dialog to show progress indicator
 * @author Brent Weatherly
 *
 */
public class GRITSProgressDialog extends Dialog implements IProgressHandler, IProgressThreadHandler
{
	private static final Logger logger = Logger.getLogger(ProgressDialog.class);
	protected TextProgressBar pbMajor; // progress bar for main thread
	protected ProgressBarWithErrorListener pblMajor; // an IProgressListener implementation
	protected GRITSWorker gritsWorker = null; // used to start work and then be notified if canceled

	protected TextProgressBar[] pbMinorDeterminants = null;  // a determinant progress bar
	protected TextProgressBar[] pbMinorIndeterminants = null; // a non-determinant progress bar
	protected ProgressBarWithErrorListener[] pblMinors = null;
	protected Composite[] minorContainers = null;
	protected StackLayout[] slMinorLayouts = null;
	protected CancelableThread cpMinorThread = null; // minor processes to be launched separate threads

	protected Text errorTextBox; // a text box for non-fatal error and warning messages

	protected int iNumMinors = 0;
	protected boolean bUseError; // tells whether to show error box
	protected boolean cancelable = true; // tells whether to show cancel button
	
	protected Button cancelButton; 
	protected Composite cancelComposite;
	protected Shell shell; //
	protected Display display = null; 
	protected int iMinorStatus;
	protected int iMajorStatus;
	

	public GRITSProgressDialog(Shell _shell, int _iNumMinors, boolean _bUseError )  
	{
		super(_shell);
		iMinorStatus = iMajorStatus = SWT.NONE;
		this.iNumMinors = _iNumMinors;
		if( this.iNumMinors > 0 ) {
			pbMinorDeterminants = new TextProgressBar[this.iNumMinors];
			pbMinorIndeterminants = new TextProgressBar[this.iNumMinors];
			pblMinors = new ProgressBarWithErrorListener[this.iNumMinors];
			minorContainers = new Composite[this.iNumMinors];
			slMinorLayouts = new StackLayout[this.iNumMinors];
		}
		this.bUseError = _bUseError;
	}
	
	public GRITSProgressDialog(Shell _shell, int _iNumMinors, boolean _bUseError, boolean cancelable ) {
		this(_shell, _iNumMinors, _bUseError);
		this.cancelable = cancelable;
	}

	public void setGritsWorker(GRITSWorker gritsWorker) {
		this.gritsWorker = gritsWorker;
	}

	public boolean isFinished() {
		boolean bFinished = this.iMajorStatus != SWT.NONE;
		return bFinished;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public ProgressBarWithErrorListener[] getMinorProgressBarListeners() {
		return pblMinors;
	}

	public ProgressBarWithErrorListener getMinorProgressBarListener(int _iInx) {
		return pblMinors[_iInx];
	}

	public ProgressBarWithErrorListener getMajorProgressBarListener() {
		return pblMajor;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public void open() {
		createContents();

		//find the center of a main monitor
		Monitor primary = shell.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		shell.open();
		shell.layout();
	}

	public int startWorker() {
		this.gritsWorker.addProgressListeners(getMajorProgressBarListener());
		int iRes = gritsWorker.doWork();
		setMajorStatus(iRes);
		Display display = getParent().getDisplay();
		while ( ! isCanceled() && ! isFinished() && ! shell.isDisposed() ) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch( Exception ex ) {
				logger.error(ex.getMessage(), ex);
			}
		}
		finish();
		return iRes;
	}

	public boolean isCanceled() {
		return getMajorStatus() == SWT.CANCEL;
	}

	public void cancelThread() {
		this.iMinorStatus = this.iMajorStatus = SWT.CANCEL;
		if( this.cpMinorThread != null ) {
			this.cpMinorThread.cancelWork();
		}
	}

	protected void createContents() {
		//shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		shell = ProgressDialog.getModalDialog(getParent());
		display = shell.getDisplay();
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;

		shell.setLayout(gridLayout);
		shell.setText("Progress Dialog");

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		composite.setLayout(new GridLayout());


		// progress indicator
		// Create a smooth progress bar
		this.pbMajor = new TextProgressBar(shell, SWT.HORIZONTAL | SWT.SMOOTH);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 500;
		this.pbMajor.setLayoutData(gd);

		for( int i = 0; i < this.iNumMinors; i++ ) {
			minorContainers[i] = new Composite(shell, SWT.NONE);
			GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
			gd2.horizontalSpan = 2;
			minorContainers[i].setLayoutData(gd2);

			slMinorLayouts[i] = new StackLayout();
			minorContainers[i].setLayout(slMinorLayouts[i]);

			this.pbMinorDeterminants[i] = new TextProgressBar(minorContainers[i], SWT.HORIZONTAL | SWT.SMOOTH);
			slMinorLayouts[i].topControl = this.pbMinorDeterminants[i];

			this.pbMinorIndeterminants[i] = new TextProgressBar(minorContainers[i], SWT.HORIZONTAL | SWT.INDETERMINATE);
			this.pbMinorIndeterminants[i].setVisible(false);
		}
		//new composite
		cancelComposite = new Composite(shell, SWT.NONE);
		cancelComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		cancelComposite.setLayout(gridLayout_1);

		if( this.bUseError ) {
			//need to create a field!
			GridData errorTextBoxGD = new GridData();
			errorTextBoxGD.minimumHeight = 100;
			errorTextBoxGD.grabExcessHorizontalSpace = true;
			errorTextBoxGD.grabExcessVerticalSpace = true;
			errorTextBoxGD.horizontalAlignment = GridData.FILL;
			errorTextBoxGD.horizontalSpan = 2;
			errorTextBoxGD.heightHint = 100;
//			errorTextBoxGD.widthHint = 500;
			errorTextBox = new Text(cancelComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
			errorTextBox.setLayoutData(errorTextBoxGD);
			errorTextBox.setEditable(false);
//			errorTextBox.s
		}
		if (cancelable) {
			cancelButton = new Button(cancelComposite, SWT.NONE);
			cancelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					//close the shell
					cancelThread();
				}
			});
			cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
			cancelButton.setText("Cancel");
		}

		this.pblMajor = new ProgressBarWithErrorListener(pbMajor, errorTextBox, display);
		for( int i = 0; i < iNumMinors; i++ ) {
			this.pblMinors[i] = new ProgressBarWithErrorListener(pbMinorDeterminants[i], pbMinorIndeterminants[i], ProgressType.Determinant, 
					slMinorLayouts[i], errorTextBox, display, shell);
		}
		shell.pack();
		shell.setSize(500, shell.getBounds().height);
	}

	/**
	 * This area is only for errors
	 * @param description
	 */
	public void setErrorText(final String a_message) {
		// create sync thread that is allow to change the display
		this.display.syncExec(new Runnable() {
			public void run() {
				StringBuffer sb = new StringBuffer();
				if(errorTextBox.getText().equals("")) {
					sb.append(a_message);
				} else {
					sb.append(errorTextBox.getText());
					sb.append("\r\n");
					sb.append(a_message);
				}
				errorTextBox.setText(sb.toString());
				//auto scroll down!!
				errorTextBox.setSelection(errorTextBox.getCharCount());
			}
		});
	}
	
	public boolean hasError() {
		if( errorTextBox == null || errorTextBox.isDisposed() ) {
			return false;
		}
		return ! errorTextBox.getText().equals("");
	}

	public Display getDisplay() {
		return display;
	}

	public void setMajorStatus( int iStatus ) {
		this.iMajorStatus = iStatus;
	}

	public int getMajorStatus() {
		return iMajorStatus;
	}

	public void setMinorStatus( int iStatus ) {
		this.iMinorStatus = iStatus;
	}

	public int getMinorStatus() {
		return iMinorStatus;
	}

	@Override
	public void threadFinished(boolean successful)
	{
		logger.debug("Thread finished. Successful: " + successful + ", is finished? " + isFinished());
		if( isCanceled() ) {
			this.display.syncExec(new Runnable() {
				public void run() {
					close();
				}
			});			
		} 
	}

	@Override
	public void endWithException(final Exception e) {
		//need to close
		this.display.syncExec(new Runnable() {
			public void run() {
				logger.error(e.getMessage(), e);
				//				if (ErrorUtils.createErrorMessageBoxReturn(shell, "Error",e) == 1)	{
				setMajorStatus(SWT.ERROR);
				setMinorStatus(SWT.ERROR);
				//then close
				close();
			}
		});
	}

	protected void finish() {
//		pblMajor.setProgressMessage("Finished");
		this.display.syncExec(new Runnable() {					
			@Override
			public void run() {
				if (cancelable) {
					cancelButton.setText("Finish");
					cancelButton.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							setMajorStatus(SWT.OK);
							setMinorStatus(SWT.OK);
							//then close
							close();
						}
					});
				} else {
					setMajorStatus(SWT.OK);
					setMinorStatus(SWT.OK);
					close();
				}
				//check if there is no error
				if( ! bUseError || errorTextBox.getText().equals(""))
				{
					setMajorStatus(SWT.OK);
					setMinorStatus(SWT.OK);
					//then close
					close();
				}
			}
		});				


	}

	@Override
	public void setThread(CancelableThread _progressThread) {
		this.cpMinorThread = _progressThread;	
	}
	
	protected void close() {
		if( !shell.isDisposed() ) {
			shell.close();
		}		
	}

}
