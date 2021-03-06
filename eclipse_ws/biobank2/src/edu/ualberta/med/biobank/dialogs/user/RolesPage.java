package edu.ualberta.med.biobank.dialogs.user;

import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.dialogs.BgcDialogPage;
import edu.ualberta.med.biobank.gui.common.dialogs.BgcDialogWithPages;
import edu.ualberta.med.biobank.gui.common.widgets.utils.TableFilter;
import edu.ualberta.med.biobank.model.Role;
import edu.ualberta.med.biobank.widgets.infotables.RoleInfoTable;
import gov.nih.nci.system.applicationservice.ApplicationException;

public abstract class RolesPage extends BgcDialogPage {
    private static final I18n i18n = I18nFactory.getI18n(RolesPage.class);

    private RoleInfoTable roleInfoTable;

    public RolesPage(BgcDialogWithPages dialog) {
        super(dialog);
    }

    @Override
    public String getTitle() {
        return Role.NAME.plural().toString();
    }

    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(1, false));

        new TableFilter<Role>(content) {
            @Override
            protected boolean accept(Role role, String text) {
                return contains(role.getName(), text);
            }

            @Override
            public List<Role> getAllCollection() {
                return getCurrentAllRolesList();
            }

            @Override
            public void setFilteredList(List<Role> filteredObjects) {
                roleInfoTable.setList(filteredObjects);
            }
        };

        roleInfoTable = new RoleInfoTable(content, null) {
            @Override
            protected boolean deleteRole(Role role) {
                boolean deleted = super.deleteRole(role);
                if (deleted) getCurrentAllRolesList().remove(role);
                return deleted;
            }

            @SuppressWarnings("nls")
            @Override
            protected void duplicate(Role src) {
                Role newRole = new Role();

                // TR: prefix of a role name when copying another role.
                String prefix = i18n.tr("Copy of ");

                newRole.setName(prefix + src.getName());
                newRole.getPermissions().addAll(src.getPermissions());
                addRole(newRole);
            }

            @Override
            protected Boolean canEdit(Role target) throws ApplicationException {
                return true;
            }

            @Override
            protected Boolean canDelete(Role target)
                throws ApplicationException {
                return true;
            }

            @Override
            protected Boolean canView(Role target) throws ApplicationException {
                return true;
            }
        };
        roleInfoTable.setList(getCurrentAllRolesList());
        setControl(content);
    }

    protected abstract List<Role> getCurrentAllRolesList();

    @Override
    public void runAddAction() {
        addRole(new Role());
    }

    @SuppressWarnings("nls")
    protected void addRole(Role role) {
        RoleEditDialog dlg = new RoleEditDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), role);
        int res = dlg.open();
        if (res == Status.OK) {
            BgcPlugin.openAsyncInformation(
                // TR: information dialog title
                i18n.tr("Role added"),
                // TR: information dialog message
                i18n.tr("Successfully added new role {0}", role.getName()));

            getCurrentAllRolesList().add(role);

            roleInfoTable.reload();
            roleInfoTable.setSelection(role);
        }
    }
}
