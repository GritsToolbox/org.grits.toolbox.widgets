package org.grits.toolbox.widgets.processDialog;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProgressBarWithErrorListener extends ProgressBarListener {
	private Text errorTextBox;
	
	public ProgressBarWithErrorListener(TextProgressBar progressBar,
			Display display) {
		super(progressBar, display);
	}
	
	public ProgressBarWithErrorListener(TextProgressBar progressBar, Text errorTextBox, Display display) {
		super(progressBar, display);
		this.errorTextBox = errorTextBox;
	}

	// option to have the ability to switch between a determinant and indeterminant progress bar
	public ProgressBarWithErrorListener(TextProgressBar progressBarDeterminant, TextProgressBar progressBarIndeterminant, ProgressType progressType, 
								StackLayout stackLayout, Text errorTextBox, Display display, Shell shell) {
		super(progressBarDeterminant, progressBarIndeterminant, progressType, stackLayout, display, shell);
		this.errorTextBox = errorTextBox;
		setProgressType(progressType);
	}
	
	/*
	 * This area is only for errors
	 * @param description
	 */
	@Override
	public void setError(String _sMessage) {
		final String t_message = new String(_sMessage);
		// create sync thread that is allow to change the display
		display.syncExec(new Runnable() 
		{
			public void run() 
			{
				if (errorTextBox != null) {
					StringBuffer sb = new StringBuffer();
					if(errorTextBox.getText().equals(""))
					{
						sb.append(t_message);
					}
					else
					{
						sb.append(errorTextBox.getText());
						sb.append("\r\n");
						sb.append(t_message);
					}
					errorTextBox.setText(sb.toString());
					//auto scroll down!!
					errorTextBox.setSelection(errorTextBox.getCharCount());
				}
			}
		});
	}
	
}
