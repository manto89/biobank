package edu.ualberta.med.biobank.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.gui.common.dialogs.BgcBaseDialog;

public abstract class PagedDialog extends BgcBaseDialog {

    protected boolean addMode;
    protected NewListener newListener;

    public PagedDialog(Shell parentShell, NewListener listener, boolean addMode) {
        super(parentShell);
        this.addMode = addMode;
        this.newListener = listener;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (addMode) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            createButton(parent, IDialogConstants.FINISH_ID, IDialogConstants.FINISH_LABEL, false);
            createButton(parent, IDialogConstants.NEXT_ID, IDialogConstants.NEXT_LABEL, true);
        } else {
            super.createButtonsForButtonBar(parent);
        }
    }

    @Override
    protected void setOkButtonEnabled(boolean enabled) {
        if (addMode) {
            Button nextButton = getButton(IDialogConstants.NEXT_ID);
            Button finishButton = getButton(IDialogConstants.FINISH_ID);
            if ((nextButton != null) && !nextButton.isDisposed()
                && (finishButton != null) && !finishButton.isDisposed()) {
                nextButton.setEnabled(enabled);
                finishButton.setEnabled(enabled);
            } else {
                okButtonEnabled = enabled;
            }
        } else {
            super.setOkButtonEnabled(enabled);
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (addMode) {
            if (IDialogConstants.CANCEL_ID == buttonId)
                super.buttonPressed(buttonId);
            else if (IDialogConstants.FINISH_ID == buttonId) {
                Button nextButton = getButton(IDialogConstants.NEXT_ID);
                if (nextButton.isEnabled()) {
                    addNew();
                    resetFields();
                }
                setReturnCode(OK);
                close();
            } else if (IDialogConstants.NEXT_ID == buttonId) {
                addNew();
                resetFields();
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }

    protected void addNew() {
        Object newObject = getNew();
        copy(newObject);
        if (newListener != null)
            newListener.newAdded(newObject);
    }

    protected abstract void copy(Object newModelObject);

    protected abstract Object getNew();

    protected abstract void resetFields();

    public void setNewListener(NewListener newListener) {
        this.newListener = newListener;
    }

    public static abstract class NewListener {
        public abstract void newAdded(Object ob);
    }

}
