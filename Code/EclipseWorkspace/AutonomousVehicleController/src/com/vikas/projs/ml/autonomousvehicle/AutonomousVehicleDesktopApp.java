package com.vikas.projs.ml.autonomousvehicle;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;

/**
 * Implements the functionality required to provide a Front End Desktop application for the Autonomous car project
 * which will perform the following:
 * 		- Stream the Video and other sensory information from the Sensor Device on the Vehicle
 * 		- Allow User to train the Autonomous Vehicle by providing directional inputs in response
 * 		  to the displayed sensory data
 * 		- Process sensory information from the vehicle and take decisions to steer by making use of 
 * 		  trained machine learning algorithms  
 * 
 * @author Vikas K Vijayakumar (kvvikas@yahoo.co.in)
 * @version 1.0
 * 
 */
public class AutonomousVehicleDesktopApp {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AutonomousVehicleDesktopApp window = new AutonomousVehicleDesktopApp();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setLayout(new GridLayout(1, false));
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		GridData gd_tabFolder = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_tabFolder.widthHint = 411;
		gd_tabFolder.heightHint = 232;
		tabFolder.setLayoutData(gd_tabFolder);
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("New Item");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(composite);
		composite.setLayout(new GridLayout(6, false));
		new Label(composite, SWT.NONE);
		
		Canvas canvas = new Canvas(composite, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		new Label(composite, SWT.NONE);
		
		Canvas canvas_1 = new Canvas(composite, SWT.NONE);
		canvas_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		new Label(composite, SWT.NONE);
		
		Canvas canvas_2 = new Canvas(composite, SWT.NONE);
		canvas_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnNewButton.setText("New Button");
		new Label(composite, SWT.NONE);
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnNewButton_1.setText("New Button");
		new Label(composite, SWT.NONE);
		
		Button btnNewButton_2 = new Button(composite, SWT.NONE);
		btnNewButton_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnNewButton_2.setText("New Button");
		
		TabItem tabItem_1 = new TabItem(tabFolder, SWT.NONE);
		tabItem_1.setText("New Item");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tabItem_1.setControl(composite_1);
		composite_1.setLayout(new GridLayout(4, false));
		new Label(composite_1, SWT.NONE);
		
		Canvas canvas_3 = new Canvas(composite_1, SWT.NONE);
		canvas_3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		new Label(composite_1, SWT.NONE);
		
		Canvas canvas_4 = new Canvas(composite_1, SWT.NONE);
		canvas_4.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		
		Button btnNewButton_3 = new Button(composite_1, SWT.NONE);
		btnNewButton_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnNewButton_3.setText("New Button");
		new Label(composite_1, SWT.NONE);
		
		Button btnNewButton_4 = new Button(composite_1, SWT.NONE);
		btnNewButton_4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnNewButton_4.setText("New Button");

	}
}
