package ru.rostvertolplc.osapr.tc08.components;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SelectDialog extends TitleAreaDialog {

	private String selectedButton;
	private String title, bodyMsg;
	private String[] listCapt;
	private int msgType; // IMessageProvider
	private int m_selectionIndex;
	private boolean m_inclSubfolders;
	private Combo comboCaptions;
	private Button buttonIncludeSub;

	/**
	 * @param parentShell
	 *            - Parent Shell
	 * @param title
	 *            - Title of the dialogue.
	 * @param bodyMsg
	 *            - Body message of the dialogue.
	 * 
	 * @param msgType
	 *            - 'IMessageProvider.INFORMATION ' Can be one of: NONE ERROR
	 *            INFORMATION WARNING
	 */
	public SelectDialog(Shell parentShell, String title, String bodyMsg,
			String[] listCapt, int msgType) { // for type see:
		// IMessageProvider

		super(parentShell);

		// Set labels.
		this.title = title;
		this.bodyMsg = bodyMsg;
		this.listCapt = listCapt;
		this.m_inclSubfolders = false;

		// set type
		this.msgType = msgType;

		// avoid help button poping up.
		this.setHelpAvailable(false);
		selectedButton = null;
	}

	/** Dialogue constructor */
	@Override
	public void create() {

		super.create();

		// The 'Message' of a TitleArea dialogue only spans 1-2 lines. Then text
		// is cut off.
		// It is not very efficient for longer messages.
		// Thus we utilize it as a 'title' and instaed we appeng a label to act
		// as body. (see below).
		setMessage(this.title, this.msgType); //$NON-NLS-1$
		// setTitle(); //not used.

		// Set the size of the dialogue.
		// We avoid hard-coding size, instead we tell it to figure out the most
		// optimal size.
		// this.getShell().setSize(650, 550); //Hard-Coded = bad.
		this.getShell().setSize(getInitialSize());
	}

	/**
	 * Return the buttonID of the button that the user selected if he pressed
	 * ok.
	 * 
	 * @return ButtonID of selected button.
	 */
	public String getSelectedButton() {
		return selectedButton;
	}
	
	public int getSelectionIndex() {
		return m_selectionIndex;
	}
	
	public boolean getIncludeSubfolders() {
		return m_inclSubfolders;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);
		Label label = new Label(container, 0);
		label.setText(this.bodyMsg);
		label = new Label(container, 0);
		label.setText("");
		label = new Label(container, 0);
		label.setText("Скопировать:");
		comboCaptions = new Combo(container, SWT.BORDER | SWT.DROP_DOWN
				| SWT.V_SCROLL | SWT.READ_ONLY);		
		for (String str : listCapt) 
			comboCaptions.add(str);
		label = new Label(container, 0);
		label.setText("Включать подпапки:");
		buttonIncludeSub = new Button(container, SWT.BORDER | SWT.CHECK);
		buttonIncludeSub.addSelectionListener(new SelectionAdapter() {     
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            m_inclSubfolders =  btn.getSelection();
	        }
	    });
		
		comboCaptions.select(0);
		return area;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	protected void saveInput() {
       m_selectionIndex = comboCaptions.getSelectionIndex();
	}

	/** Called when the ok button is pressed */
	@Override
	protected void okPressed() {
		saveInput(); // save input.
		super.okPressed(); // close dialogue
	}
}