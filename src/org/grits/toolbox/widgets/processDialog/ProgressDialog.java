package org.grits.toolbox.widgets.processDialog;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.progress.ProgressThread;

/**
 * Progress Dialog to show progress indicator
 * @author kitaemyoung
 *
 */
public class ProgressDialog extends Dialog implements IProgressThreadHandler
{
	//log4J Logger
	private static final Logger logger = Logger.getLogger(ProgressDialog.class);
	protected ProgressThread m_worker = null;

	protected Label processMessageLabel;
	protected Label updateMessageLabel;
	protected Button cancelButton; 

	protected Text descriptionText;

	protected Composite cancelComposite;
	protected Shell shell; //
	protected Display display = null; 

	private int max = 0;
	private double count = 0.0;
	private double increment = 0.0;

	protected boolean isCanceled = false;

	protected ProgressBar progressBar;
	protected int progressBarStyle; 
	
	public ProgressDialog(Shell parent) 
	{
		super(parent);
		progressBarStyle = SWT.HORIZONTAL | SWT.SMOOTH;
	}
	
	public ProgressDialog(Shell parent, int style) {
		super(parent);
		progressBarStyle = style;
	}

	public Shell getShell() {
		return shell;
	}
	
	public void setShell(Shell shell) {
		this.shell = shell;
	}
		
	public ProgressThread getWorker() {
		return m_worker;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public void setWorker(ProgressThread a_worker)
	{
		this.m_worker = a_worker;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}
	
	public void setProcessMessageLabel(String a_message)
	{
		// need final variable
		final String t_message = new String(a_message);
		// create sync thread that allows to change the display
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				processMessageLabel.setText(t_message);
			}
		});
		
	}

	public void updateProgresBar(String msg) 
	{
		//need final variable
		final String t_message = new String(msg);
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				progressBar.setSelection((int)(count+increment));
				count = count + increment;
				updateMessageLabel.setText(t_message + " of " + max);
			}
		});
	}

	public int open() 
	{
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

		this.m_worker.setProgressThreadHandler(this);
		this.m_worker.start();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) 
		{
			try {
				if (!display.readAndDispatch()) 
				{
					display.sleep();
				}
			} catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
		if(isCanceled)
		{
			return SWT.CANCEL;
		}
		return SWT.OK;
	}
	
	public boolean isCanceled() {
		return isCanceled;
	}

    public static Shell getModalDialog(Shell parent)
    {
        return new Shell(parent, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.TITLE & (~SWT.RESIZE) & (~SWT.MAX) & (~SWT.MIN));
    }

	protected void createContents() 
	{
		//shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		shell = ProgressDialog.getModalDialog(getParent());
		display = shell.getDisplay();
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;

		shell.setLayout(gridLayout);
		shell.setSize(483, 300);
		shell.setText("Progress Dialog");

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		composite.setLayout(new GridLayout());

		processMessageLabel = new Label(shell, SWT.NONE);
		processMessageLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		// progress indicator
		// Create a smooth progress bar
		progressBar = new ProgressBar(shell, this.progressBarStyle);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		progressBar.setMinimum(0);
		progressBar.setMaximum(this.max);
		progressBar.setLayoutData(gd);

		updateMessageLabel = new Label(shell, SWT.NONE);
		updateMessageLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		//new composite
		cancelComposite = new Composite(shell, SWT.NONE);
		cancelComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		cancelComposite.setLayout(gridLayout_1);

		//need to create a field!
		GridData descriptionTextData = new GridData();
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.grabExcessVerticalSpace = true;
		descriptionTextData.horizontalAlignment = GridData.FILL;
		descriptionTextData.horizontalSpan = 2;
		descriptionText = new Text(cancelComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		descriptionText.setLayoutData(descriptionTextData);
		descriptionText.setEditable(false);

		cancelButton = new Button(cancelComposite, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCanceled = true;
				//close the shell
				clickCancel();
			}
		});
		cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
		cancelButton.setText("cancel");
	}

	protected void clickCancel() {
		//notify worker
		this.m_worker.cancelWork();
	}

	/**
	 * This area is only for errors
	 * @param description
	 */
	public void setDescriptionText(String description) {
		final String t_message = new String(description);
		// create sync thread that is allow to change the display
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				StringBuffer sb = new StringBuffer();
				if(descriptionText.getText().equals(""))
				{
					sb.append(t_message);
				}
				else
				{
					sb.append(descriptionText.getText());
					sb.append("\r\n");
					sb.append(t_message);
				}
				descriptionText.setText(sb.toString());
				//auto scroll down!!
				descriptionText.setSelection(descriptionText.getCharCount());
			}
		});
	}

	public void setMax(int max) {
		this.max = max;
		this.increment = 100.00/(double)max;
		//start from beginning
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				progressBar.setSelection(0);
				count = 0;
			}
		});
	}

	public Display getDisplay() {
		return display;
	}

	public void threadFinished(boolean successful)
	{
		if (!isCanceled && successful)
		{
			// called by the new thread shortly before finishing
			// should trigger the finish button to become available
			this.display.syncExec(new Runnable() 
			{
				public void run() 
				{
					progressBar.setSelection(100);
					cancelButton.setText("Finish");
					cancelButton.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							isCanceled = false;
							//then close
							shell.close();
						}
					});
					//check if there is no error
					if(descriptionText.getText().equals(""))
					{
						//then automatically close the dialog
						isCanceled = false;
						//then close
						shell.close();
					}
					
				}
			});
		}
		else
		{
			this.display.syncExec(new Runnable() 
			{
				public void run() 
				{
					isCanceled = true;
					//then close
					shell.close();
				}
			});
		}
	}

	public void endWithException(final Exception e) {
		//need to close
		this.display.syncExec(new Runnable()
		{
			public void run() 
			{
				logger.error(e.getMessage(), e);
//				if (ErrorUtils.createErrorMessageBoxReturn(shell, "Error",e) == 1) {
					isCanceled = true;
					//then close
					shell.close();
//				}
			}
		});
	}

	@Override
	public void cancelThread() {
		// TODO Auto-generated method stub
		
	}
}
