package edu.ualberta.med.biobank.treeview.dispatch;

import java.util.Collection;
import java.util.Date;

import org.acegisecurity.AccessDeniedException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.DispatchState;
import edu.ualberta.med.biobank.common.wrappers.DispatchWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.forms.DispatchReceivingEntryForm;
import edu.ualberta.med.biobank.forms.DispatchSendingEntryForm;
import edu.ualberta.med.biobank.forms.DispatchViewForm;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.views.DispatchAdministrationView;

public class DispatchAdapter extends AdapterBase {

    public DispatchAdapter(AdapterBase parent, DispatchWrapper ship) {
        super(parent, ship);
    }

    public DispatchWrapper getWrapper() {
        return (DispatchWrapper) modelObject;
    }

    @Override
    public boolean isEditable() {
        boolean editable = super.isEditable();
        if (getWrapper() != null) {
            return editable
                && (getWrapper().isNew() || !getWrapper().isInTransitState());
        }
        return editable;
    }

    @Override
    protected String getLabelInternal() {
        DispatchWrapper shipment = getWrapper();
        Assert.isNotNull(shipment, "Dispatch is null");
        String label = new String();
        label += " - ";

        label += shipment.getFormattedDeparted();
        return label;

    }

    @Override
    public String getTooltipText() {
        return getTooltipText("Dispatch");
    }

    @Override
    public boolean isDeletable() {
        if (getSiteParent() != null)
            return getSiteParent().equals(getWrapper().getSender())
                && getWrapper().canDelete(SessionManager.getUser())
                && getWrapper().isInCreationState();
        else
            return false;
    }

    private SiteWrapper getSiteParent() {
        if (getParent().getParent().getParent() != null)
            return (SiteWrapper) getParent().getParent().getParent()
                .getModelObject();
        return null;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        SiteWrapper siteParent = getSiteParent();
        addViewMenu(menu, "Dispatch");
        try {
            if (isDeletable()) {
                addDeleteMenu(menu, "Dispatch");
            }
            if (siteParent.equals(getWrapper().getSender())
                && getWrapper().canUpdate(SessionManager.getUser())
                && getWrapper().isInTransitState()) {
                MenuItem mi = new MenuItem(menu, SWT.PUSH);
                mi.setText("Move to Creation");
                mi.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        setDispatchAsCreation();
                    }
                });
            }
            if (siteParent.equals(getWrapper().getReceiver())
                && getWrapper().isInTransitState()) {
                MenuItem mi = new MenuItem(menu, SWT.PUSH);
                mi.setText("Receive");
                mi.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        doReceive();
                    }
                });
                mi = new MenuItem(menu, SWT.PUSH);
                mi.setText("Receive and Process");
                mi.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        doReceiveAndProcess();
                    }
                });
            } else if (getWrapper().canUpdate(SessionManager.getUser())) {
                addEditMenu(menu, "Dispatch");
            }
        } catch (Exception e) {
            BioBankPlugin.openAsyncError("Error checking permissions", e);
        }
    }

    @Override
    protected String getConfirmDeleteMessage() {
        return "Are you sure you want to delete this dispatch?";
    }

    public void doReceive() {
        setDispatchAsReceived();
        openViewForm();
    }

    public void doReceiveAndProcess() {
        setDispatchAsReceived();
        openEntryForm();
    }

    public void doClose() {
        getWrapper().setState(DispatchState.CLOSED);
        persistDispatch();
        openViewForm();
    }

    public void doSetAsLost() {
        getWrapper().setState(DispatchState.LOST);
        persistDispatch();
        openViewForm();
    }

    private void setDispatchAsReceived() {
        getWrapper().setDateReceived(new Date());
        getWrapper().setState(DispatchState.RECEIVED);
        persistDispatch();
    }

    private void setDispatchAsCreation() {
        getWrapper().setState(DispatchState.CREATION);
        getWrapper().setDeparted(null);
        persistDispatch();
    }

    private void persistDispatch() {
        try {
            getWrapper().persist();
        } catch (final RemoteConnectFailureException exp) {
            BioBankPlugin.openRemoteConnectErrorMessage(exp);
        } catch (final RemoteAccessException exp) {
            BioBankPlugin.openRemoteAccessErrorMessage(exp);
        } catch (final AccessDeniedException ade) {
            BioBankPlugin.openAccessDeniedErrorMessage(ade);
        } catch (Exception ex) {
            BioBankPlugin.openAsyncError("Save error", ex);
        }
        DispatchAdministrationView.getCurrent().reload();
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return null;
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return null;
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return 0;
    }

    @Override
    public String getViewFormId() {
        return DispatchViewForm.ID;
    }

    @Override
    public String getEntryFormId() {
        if (getWrapper().isInCreationState())
            return DispatchSendingEntryForm.ID;
        return DispatchReceivingEntryForm.ID;
    }

}
