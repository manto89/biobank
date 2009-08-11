package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.forms.StudyEntryForm;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.SessionAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;

public class StudyAddHandler extends AbstractHandler {
    public static final String ID = "edu.ualberta.med.biobank.commands.addStudy";

    public Object execute(ExecutionEvent event) throws ExecutionException {
        SessionAdapter sessionAdapter = SessionManager.getInstance()
            .getSession();
        Assert.isNotNull(sessionAdapter);

        Study study = new Study();
        StudyAdapter studyNode = new StudyAdapter(sessionAdapter, study);

        FormInput input = new FormInput(studyNode);
        try {
            HandlerUtil.getActiveWorkbenchWindowChecked(event).getActivePage()
                .openEditor(input, StudyEntryForm.ID, true);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
