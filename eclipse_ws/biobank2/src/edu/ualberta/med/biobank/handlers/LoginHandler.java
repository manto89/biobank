package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.dialogs.ChangePasswordDialog;
import edu.ualberta.med.biobank.dialogs.startup.LoginDialog;
import edu.ualberta.med.biobank.treeview.admin.SessionAdapter;

public class LoginHandler extends AbstractHandler implements IHandler {

    public static final String ID = "edu.ualberta.med.biobank.commands.login"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        LoginDialog loginDialog = new LoginDialog(
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        if (loginDialog.open() == Dialog.OK) {
            SessionAdapter session = SessionManager.getInstance().getSession();
            if ((session != null) && session.getUser().needChangePassword()) {
                ChangePasswordDialog.open(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    true);
            }
        }
        return null;
    }
}
