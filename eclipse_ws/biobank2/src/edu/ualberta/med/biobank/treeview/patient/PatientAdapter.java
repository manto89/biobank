package edu.ualberta.med.biobank.treeview.patient;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.patient.PatientDeleteAction;
import edu.ualberta.med.biobank.common.action.patient.PatientGetSimpleCollectionEventInfosAction;
import edu.ualberta.med.biobank.common.action.patient.PatientGetSimpleCollectionEventInfosAction.SimpleCEventInfo;
import edu.ualberta.med.biobank.common.action.patient.PatientSearchAction.SearchedPatientInfo;
import edu.ualberta.med.biobank.common.permission.patient.PatientDeletePermission;
import edu.ualberta.med.biobank.common.permission.patient.PatientReadPermission;
import edu.ualberta.med.biobank.common.permission.patient.PatientUpdatePermission;
import edu.ualberta.med.biobank.forms.PatientEntryForm;
import edu.ualberta.med.biobank.forms.PatientViewForm;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AbstractNewAdapterBase;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class PatientAdapter extends AbstractNewAdapterBase {

    private Patient patient;
    private Study study;

    @SuppressWarnings("unused")
    private Long ceventsCount;

    public PatientAdapter(AbstractAdapterBase parent, SearchedPatientInfo pinfo) {
        super(parent, pinfo == null ? null : pinfo.patient.getId(), null, null,
            (pinfo == null || pinfo.ceventsCount == null) ? false
                : pinfo.ceventsCount > 0);
        setValue(pinfo);

    }

    @Override
    public void init() {
        try {
            this.isDeletable =
                SessionManager.getAppService().isAllowed(
                    new PatientDeletePermission(patient.getId()));
            this.isReadable =
                SessionManager.getAppService().isAllowed(
                    new PatientReadPermission(patient.getId()));
            this.isEditable =
                SessionManager.getAppService().isAllowed(
                    new PatientUpdatePermission(patient.getId()));
        } catch (ApplicationException e) {
            BgcPlugin.openAsyncError("Permission Error",
                "Unable to retrieve user permissions");
        }
    }

    @Override
    protected String getLabelInternal() {
        if (patient == null)
            return "no patient - should not see this"; //$NON-NLS-1$
        return patient.getPnumber();
    }

    @Override
    public String getTooltipTextInternal() {
        if (patient != null && study != null)
            return study.getName()
                + " - " + getTooltipText(Messages.PatientAdapter_patient_label); //$NON-NLS-1$
        return getTooltipText(Messages.PatientAdapter_patient_label);
    }

    @Override
    public void executeDoubleClick() {
        performExpand();
        openViewForm();
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        addEditMenu(menu, Messages.PatientAdapter_patient_label);
        addViewMenu(menu, Messages.PatientAdapter_patient_label);
        addDeleteMenu(menu, Messages.PatientAdapter_patient_label);

        if (isEditable()) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText(Messages.PatientAdapter_add_cevent_label);
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    SimpleCEventInfo cevent = new SimpleCEventInfo();
                    cevent.cevent = new CollectionEvent();
                    cevent.cevent.setPatient(patient);
                    CollectionEventAdapter ceventAdapter =
                        new CollectionEventAdapter(
                            null, cevent);
                    ceventAdapter.openEntryForm();
                }
            });
        }
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        return findChildFromClass(searchedClass, objectId,
            CollectionEvent.class);
    }

    @Override
    protected CollectionEventAdapter createChildNode() {
        return new CollectionEventAdapter(this, null);
    }

    @Override
    protected CollectionEventAdapter createChildNode(Object child) {
        Assert.isTrue(child instanceof SimpleCEventInfo);
        return new CollectionEventAdapter(this, (SimpleCEventInfo) child);
    }

    @Override
    protected Map<Integer, ?> getChildrenObjects() throws Exception {
        return SessionManager.getAppService().doAction(
            new PatientGetSimpleCollectionEventInfosAction(patient.getId()))
            .getMap();
    }

    @Override
    public String getEntryFormId() {
        return PatientEntryForm.ID;
    }

    @Override
    public String getViewFormId() {
        return PatientViewForm.ID;
    }

    @Override
    protected String getConfirmDeleteMessage() {
        return Messages.PatientAdapter_delete_confirm_msg;
    }

    public Patient getPatient() {
        return patient;
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        if (o instanceof PatientAdapter)
            return patient.getPnumber().compareTo(
                ((PatientAdapter) o).patient.getPnumber());
        return 0;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof SearchedPatientInfo) {
            SearchedPatientInfo pinfo = (SearchedPatientInfo) value;
            this.patient = pinfo.patient;
            this.study = pinfo.study;
            this.ceventsCount = pinfo.ceventsCount;
            if (patient.getId() != null) {
                setId(patient.getId());
                init();
            }
        }

    }

    @Override
    protected void runDelete() throws Exception {
        SessionManager.getAppService().doAction(
            new PatientDeleteAction(patient));
    }

}
