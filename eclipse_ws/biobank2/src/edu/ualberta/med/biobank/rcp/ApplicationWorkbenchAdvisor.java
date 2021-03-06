package edu.ualberta.med.biobank.rcp;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.client.util.ServiceConnection;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.rcp.perspective.LinkAssignPerspective;
import edu.ualberta.med.biobank.rcp.perspective.MainPerspective;
import edu.ualberta.med.biobank.rcp.perspective.ReportsPerspective;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
    private static final I18n i18n = I18nFactory
        .getI18n(ApplicationWorkbenchAdvisor.class);

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
        IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return MainPerspective.ID;
    }

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        configurer.setSaveAndRestore(true);
    }

    @SuppressWarnings("nls")
    @Override
    public boolean preShutdown() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page.getPerspective().getId().equals(ReportsPerspective.ID)) {
                IPerspectiveDescriptor main = workbench
                    .getPerspectiveRegistry().findPerspectiveWithId(
                        MainPerspective.ID);
                page.setPerspective(main);
            }
            if (BiobankPlugin.isAskPrintActivityLog()
                && page.getPerspective().getId()
                    .equals(LinkAssignPerspective.ID)) {
                BgcPlugin
                    .openInformation(
                        i18n.tr("Can't close"),
                        i18n.tr("Please end specimen management session before closing"));
                return false;
            }
        }
        if (SessionManager.getInstance().isConnected()) {
            try {
                ServiceConnection.logout(SessionManager.getAppService());
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public void postStartup() {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        // remove the default 'General' preference page of the workbench
        pm.remove("org.eclipse.ui.preferencePages.Workbench");
    }
}
