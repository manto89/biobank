package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.Section;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventGetInfoAction;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventGetInfoAction.CEventInfo;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventSaveAction;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventSaveAction.CEventAttrSaveInfo;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventSaveAction.SaveCEventSpecimenInfo;
import edu.ualberta.med.biobank.common.action.collectionEvent.EventAttrInfo;
import edu.ualberta.med.biobank.common.action.eventattr.EventAttrTypeEnum;
import edu.ualberta.med.biobank.common.action.patient.PatientGetSimpleCollectionEventInfosAction;
import edu.ualberta.med.biobank.common.action.patient.PatientGetSimpleCollectionEventInfosAction.SimpleCEventInfo;
import edu.ualberta.med.biobank.common.action.patient.PatientNextVisitNumberAction;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenInfo;
import edu.ualberta.med.biobank.common.action.specimenType.SpecimenTypeGetAllAction;
import edu.ualberta.med.biobank.common.action.study.StudyEventAttrInfo;
import edu.ualberta.med.biobank.common.action.study.StudyGetEventAttrInfoAction;
import edu.ualberta.med.biobank.common.action.study.StudyGetSourceSpecimensAction;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.peer.CollectionEventPeer;
import edu.ualberta.med.biobank.common.peer.PatientPeer;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.CommentWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.dialogs.BiobankWizardDialog;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.gui.common.widgets.BgcEntryFormWidgetListener;
import edu.ualberta.med.biobank.gui.common.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.gui.common.widgets.MultiSelectEvent;
import edu.ualberta.med.biobank.gui.common.widgets.utils.BgcWidgetCreator;
import edu.ualberta.med.biobank.gui.common.widgets.utils.ComboSelectionUpdate;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.EventAttrCustom;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.SourceSpecimen;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.patient.CollectionEventAdapter;
import edu.ualberta.med.biobank.validators.DoubleNumberValidator;
import edu.ualberta.med.biobank.validators.IntegerNumberValidator;
import edu.ualberta.med.biobank.widgets.ComboAndQuantityWidget;
import edu.ualberta.med.biobank.widgets.SelectMultipleWidget;
import edu.ualberta.med.biobank.widgets.infotables.CommentsInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.NewSpecimenInfoTable.ColumnsShown;
import edu.ualberta.med.biobank.widgets.infotables.entry.CEventSpecimenEntryInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.entry.CommentedSpecimenInfo;
import edu.ualberta.med.biobank.widgets.utils.GuiUtil;
import edu.ualberta.med.biobank.wizards.RepatientingWizard;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class CollectionEventEntryForm extends BiobankEntryForm {
    private static final I18n i18n = I18nFactory
        .getI18n(CollectionEventEntryForm.class);

    @SuppressWarnings("nls")
    public static final String ID =
        "edu.ualberta.med.biobank.forms.CollectionEventEntryForm";

    @SuppressWarnings("nls")
    public static final String MSG_NEW_PATIENT_VISIT_OK =
        "Creating a new patient visit record.";

    @SuppressWarnings("nls")
    public static final String MSG_PATIENT_VISIT_OK =
        "Editing an existing patient visit record.";

    private CollectionEvent ceventCopy;

    private static class FormPvCustomInfo extends EventAttrCustom {
        private Control control;
    }

    private List<FormPvCustomInfo> pvCustomInfoList;

    private final BgcEntryFormWidgetListener listener =
        new BgcEntryFormWidgetListener() {
            @Override
            public void selectionChanged(MultiSelectEvent event) {
                setDirty(true);
            }
        };

    private ComboViewer activityStatusComboViewer;

    private CEventSpecimenEntryInfoTable specimensTable;
    private BgcBaseText visitNumberText;

    private DateTimeWidget timeDrawnWidget;

    private List<SpecimenInfo> sourceSpecimens;

    private CEventInfo ceventInfo;

    private CommentsInfoTable commentEntryTable;

    private final CommentWrapper comment = new CommentWrapper(
        SessionManager.getAppService());

    Map<Integer, StudyEventAttrInfo> studyAttrInfos;

    private final CollectionEventWrapper cevent = new CollectionEventWrapper(
        SessionManager.getAppService());

    protected RepatientingWizard wizard;

    private BgcBaseText patientField;

    @SuppressWarnings("nls")
    @Override
    public void init() throws Exception {
        Assert.isTrue(adapter instanceof CollectionEventAdapter,
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        ceventCopy = new CollectionEvent();
        if (adapter.getId() == null) {
            ceventInfo = new CEventInfo();
            ceventInfo.cevent = new CollectionEvent();
            ceventInfo.cevent.setPatient(((CollectionEventAdapter) adapter)
                .getPatient());
        } else {
            ceventInfo = SessionManager.getAppService().doAction(
                new CollectionEventGetInfoAction(adapter.getId()));
        }
        studyAttrInfos = SessionManager.getAppService().doAction(
            new StudyGetEventAttrInfoAction(ceventInfo.cevent
                .getPatient().getStudy().getId())).getMap();
        copyCEvent();
        String tabName;
        if (adapter.getId() == null) {
            // tab name
            tabName = i18n.tr("New collection event");
        } else {
            // tab name
            tabName =
                i18n.tr("Collection Event - #{0}", ceventCopy.getVisitNumber());
        }

        setPartName(tabName);
    }

    private void copyCEvent() throws Exception {
        // only value created when is a new cevent.
        ceventCopy.setPatient(ceventInfo.cevent.getPatient());
        if (adapter.getId() == null) {
            ceventCopy.setVisitNumber(SessionManager
                .getAppService()
                .doAction(
                    new PatientNextVisitNumberAction(ceventInfo.cevent
                        .getPatient().getId())).getNextVisitNumber());
            ceventCopy.setActivityStatus(ActivityStatus.ACTIVE);
            sourceSpecimens = new ArrayList<SpecimenInfo>();
        } else {
            ceventCopy.setId(ceventInfo.cevent.getId());
            ceventCopy.setVisitNumber(ceventInfo.cevent.getVisitNumber());
            ceventCopy.setActivityStatus(ceventInfo.cevent.getActivityStatus());
            ceventCopy.setComments(ceventInfo.cevent.getComments());
            sourceSpecimens =
                new ArrayList<SpecimenInfo>(
                );
            for (SpecimenInfo info : ceventInfo.sourceSpecimenInfos)
                sourceSpecimens.add(new CommentedSpecimenInfo(info));
        }
        cevent.setWrappedObject(ceventCopy);
        comment.setWrappedObject(new Comment());
    }

    @SuppressWarnings("nls")
    @Override
    protected void createFormContent() throws Exception {
        form.setText(
            // form title
            i18n.tr("Collection Event Information"));
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        page.setLayout(new GridLayout(1, false));
        createMainSection();
        createCommentSection();
        createSpecimensSection();
        if (adapter.getId() == null) {
            setDirty(true);
        }
    }

    @SuppressWarnings("nls")
    private void createCommentSection() {
        Composite client =
            createSectionWithClient(Comment.NAME.format(2).toString());
        GridLayout gl = new GridLayout(2, false);

        client.setLayout(gl);
        commentEntryTable =
            new CommentsInfoTable(client, cevent.getCommentCollection(true));
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        commentEntryTable.setLayoutData(gd);
        createBoundWidgetWithLabel(client, BgcBaseText.class, SWT.MULTI,
            // label
            i18n.tr("Add a comment"),
            null, comment, "message", null);

    }

    @SuppressWarnings("nls")
    private void createMainSection() throws Exception {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        createReadOnlyLabelledField(client, SWT.NONE,
            Study.NAME.format(1).toString(), cevent
                .getPatient().getStudy().getName());

        widgetCreator.createLabel(client,
            Patient.NAME.singular().toString());

        Composite c = new Composite(client, SWT.NONE);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        c.setLayoutData(gd);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        c.setLayout(gl);

        patientField =
            (BgcBaseText) widgetCreator.createBoundWidget(
                c,
                BgcBaseText.class,
                SWT.READ_ONLY,
                null,
                BeansObservables
                    .observeValue(cevent,
                        Property.concatNames(CollectionEventPeer.PATIENT,
                            PatientPeer.PNUMBER)), null);
        patientField.setBackground(BgcWidgetCreator.READ_ONLY_TEXT_BGR);

        Button editSourceButton = new Button(c, SWT.NONE);
        editSourceButton
            .setText(i18n.tr("Change Source"));

        toolkit.adapt(c);

        editSourceButton.addListener(SWT.MouseUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                wizard =
                    new RepatientingWizard(
                        SessionManager.getAppService());
                WizardDialog dialog = new BiobankWizardDialog(page.getShell(),
                    wizard);
                int res = dialog.open();
                if (res == Status.OK) {
                    cevent.setPatient(wizard.getPatient());
                    comment.setMessage(wizard.getComment());
                    setDirty(true);
                }
            }
        });

        visitNumberText =
            (BgcBaseText) createBoundWidgetWithLabel(
                client,
                BgcBaseText.class,
                SWT.NONE,
                CollectionEvent.PropertyName.VISIT_NUMBER.toString(),
                null,
                cevent,
                CollectionEventPeer.VISIT_NUMBER.getName(),
                new IntegerNumberValidator(
                    // validation error message
                    i18n.tr("Visit must have a number"),
                    false));

        visitNumberText.addSelectionChangedListener(listener);
        setFirstControl(visitNumberText);

        activityStatusComboViewer =
            createComboViewer(
                client,
                ActivityStatus.NAME.format(1).toString(),
                ActivityStatus.valuesList(),
                cevent.getActivityStatus(),
                // validation error message
                i18n.tr("Patient visit must have an activity status"),
                new ComboSelectionUpdate() {
                    @Override
                    public void doSelection(Object selectedObject) {
                        if (selectedObject != cevent.getActivityStatus()) {
                            setDirty(true);
                        }
                        cevent
                            .setActivityStatus((ActivityStatus) selectedObject);
                    }
                });

        widgetCreator.createLabel(client,
            // label
            i18n.tr("Time drawn to use \non new specimens"));
        timeDrawnWidget =
            new DateTimeWidget(client, SWT.DATE | SWT.TIME, new Date());
        toolkit.adapt(timeDrawnWidget);

        createEventAttrSection(client);
    }

    private void createEventAttrSection(Composite client) throws Exception {
        pvCustomInfoList = new ArrayList<FormPvCustomInfo>();

        for (Entry<Integer, StudyEventAttrInfo> entry : studyAttrInfos
            .entrySet()) {
            FormPvCustomInfo pvCustomInfo = new FormPvCustomInfo();
            pvCustomInfo.setStudyEventAttrId(entry.getValue().attr.getId());
            pvCustomInfo.setLabel(entry.getValue().attr.getGlobalEventAttr()
                .getLabel());
            pvCustomInfo.setType(entry.getValue().type);
            pvCustomInfo.setAllowedValues(entry.getValue()
                .getStudyEventAttrPermissible());
            // FIXME ugly
            EventAttrInfo eventAttrInfo = (adapter.getId() == null)
                ? null : ceventInfo.eventAttrs.get(entry.getKey());
            String origValue = (eventAttrInfo == null)
                ? StringUtil.EMPTY_STRING : eventAttrInfo.attr.getValue();
            pvCustomInfo.setValue(origValue);
            pvCustomInfo.setOrigValue(origValue);
            pvCustomInfo.control = getControlForLabel(client, pvCustomInfo);
            pvCustomInfoList.add(pvCustomInfo);
        }
    }

    @SuppressWarnings("nls")
    private void createSpecimensSection() {
        Section section = createSection(SourceSpecimen.NAME.format(2).toString());
        specimensTable = new CEventSpecimenEntryInfoTable(section, sourceSpecimens,
            ceventCopy, ColumnsShown.CEVENT_SOURCE_SPECIMENS);
        specimensTable.adaptToToolkit(toolkit, true);
        specimensTable.addSelectionChangedListener(listener);
        try {
            final List<SpecimenType> allSpecimenTypes = SessionManager.getAppService().doAction(
                new SpecimenTypeGetAllAction()).getList();
            final Set<SourceSpecimen> studySourceSpecimens = SessionManager.getAppService().doAction(
                new StudyGetSourceSpecimensAction(
                    ceventInfo.cevent.getPatient().getStudy().getId(), true)).getSet();

            specimensTable.addEditSupport(studySourceSpecimens, allSpecimenTypes);
            addSectionToolbar(section,
                // label
                i18n.tr("Add specimens"),
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        form.setFocus();
                        specimensTable.addOrEditSpecimen(true, null,
                            studySourceSpecimens, allSpecimenTypes, ceventCopy,
                            timeDrawnWidget.getDate());
                    }
                });
        } catch (ApplicationException e) {
            BgcPlugin.openAsyncError(
                // dialog title
                i18n.tr("Error retrieving source specimens"), e);
        }
        section.setClient(specimensTable);
    }

    @SuppressWarnings("nls")
    private Control getControlForLabel(Composite client,
        FormPvCustomInfo pvCustomInfo) {
        Control control;
        if (EventAttrTypeEnum.NUMBER == pvCustomInfo.getType()) {
            control = createBoundWidgetWithLabel(
                client, BgcBaseText.class, SWT.NONE, pvCustomInfo.getLabel(),
                null, pvCustomInfo, FormPvCustomInfo.VALUE_BIND_STRING,
                new DoubleNumberValidator(
                    // validation error message
                    i18n.tr("You should select a valid number")));
        } else if (EventAttrTypeEnum.TEXT == pvCustomInfo.getType()) {
            control =
                createBoundWidgetWithLabel(client, BgcBaseText.class, SWT.NONE,
                    pvCustomInfo.getLabel(), null, pvCustomInfo,
                    FormPvCustomInfo.VALUE_BIND_STRING, null);
        } else if (EventAttrTypeEnum.DATE_TIME == pvCustomInfo.getType()) {
            control =
                createDateTimeWidget(client, pvCustomInfo.getLabel(),
                    DateFormatter.parseToDateTime(pvCustomInfo.getValue()),
                    null, null);
        } else if (EventAttrTypeEnum.SELECT_SINGLE == pvCustomInfo.getType()) {
            control =
                createBoundWidgetWithLabel(client, Combo.class, SWT.NONE,
                    pvCustomInfo.getLabel(), pvCustomInfo.getAllowedValues(),
                    pvCustomInfo, FormPvCustomInfo.VALUE_BIND_STRING, null);
        } else if (EventAttrTypeEnum.SELECT_MULTIPLE == pvCustomInfo.getType()) {
            widgetCreator.createLabel(client, pvCustomInfo.getLabel());
            SelectMultipleWidget s =
                new SelectMultipleWidget(client, SWT.BORDER,
                    pvCustomInfo.getAllowedValues(), selectionListener);
            s.adaptToToolkit(toolkit, true);
            if (pvCustomInfo.getValue() != null) {
                s.setSelections(pvCustomInfo.getValue().split(
                    FormPvCustomInfo.VALUE_MULTIPLE_SEPARATOR));
            }
            control = s;
        } else {
            Assert.isTrue(false,
                "Invalid pvInfo type: " + pvCustomInfo.getType());
            return null;
        }
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        control.setLayoutData(gd);
        return control;
    }

    @SuppressWarnings("nls")
    private void updateControlForLabel(FormPvCustomInfo pvCustomInfo) {
        if ((EventAttrTypeEnum.NUMBER == pvCustomInfo.getType())
            || (EventAttrTypeEnum.TEXT == pvCustomInfo.getType()))
            ((BgcBaseText) pvCustomInfo.control).setText(pvCustomInfo
                .getOrigValue());
        else if (EventAttrTypeEnum.DATE_TIME == pvCustomInfo.getType())
            ((DateTimeWidget) pvCustomInfo.control).setDate(DateFormatter
                .parseToDateTime(pvCustomInfo.getOrigValue()));
        else if (EventAttrTypeEnum.SELECT_SINGLE == pvCustomInfo.getType())
            ((Combo) pvCustomInfo.control).setText(pvCustomInfo.getOrigValue());
        else if (EventAttrTypeEnum.SELECT_MULTIPLE == pvCustomInfo.getType()) {
            ((SelectMultipleWidget) pvCustomInfo.control)
                .setSelections(pvCustomInfo.getOrigValue().split(";"));
        } else {
            Assert.isTrue(false,
                "Invalid pvInfo type: " + pvCustomInfo.getType());
        }
    }

    @Override
    protected String getOkMessage() {
        return (adapter.getId() == null) ? MSG_NEW_PATIENT_VISIT_OK
            : MSG_PATIENT_VISIT_OK;
    }

    @Override
    protected void doBeforeSave() throws Exception {
        for (FormPvCustomInfo combinedPvInfo : pvCustomInfoList) {
            // set the value from the widget
            savePvInfoValueFromControlType(combinedPvInfo);
        }
    }

    @Override
    protected void saveForm() throws Exception {
        Assert.isNotNull(SessionManager.getUser().getCurrentWorkingCenter());
        Set<SaveCEventSpecimenInfo> spcInfo = new HashSet<SaveCEventSpecimenInfo>();
        for (SpecimenInfo o : specimensTable.getList()) {
            CommentedSpecimenInfo specInfo = (CommentedSpecimenInfo) o;
            spcInfo.add(new SaveCEventSpecimenInfo(
                specInfo.specimen.getId(), specInfo.specimen.getInventoryId(),
                specInfo.specimen.getCreatedAt(), specInfo.specimen.getActivityStatus(),
                specInfo.specimen.getSpecimenType().getId(),
                specInfo.comments, specInfo.specimen.getQuantity()));
        }

        List<CEventAttrSaveInfo> ceventAttrList = new ArrayList<CEventAttrSaveInfo>();
        for (FormPvCustomInfo combinedPvInfo : pvCustomInfoList) {
            String value = combinedPvInfo.getValue().trim();
            if (!value.isEmpty()) {
                ceventAttrList.add(new CEventAttrSaveInfo(
                    combinedPvInfo.getStudyEventAttrId(), combinedPvInfo.getType(), value));
            }
        }

        // save the collection event
        Integer savedCeventId = SessionManager.getAppService().doAction(
            new CollectionEventSaveAction(ceventCopy.getId(),
                ceventCopy.getPatient().getId(), ceventCopy.getVisitNumber(),
                ceventCopy.getActivityStatus(), comment.getMessage(), spcInfo, ceventAttrList,
                SessionManager.getUser().getCurrentWorkingCenter().getWrappedObject())
            ).getId();
        PatientGetSimpleCollectionEventInfosAction action =
            new PatientGetSimpleCollectionEventInfosAction(ceventCopy
                .getPatient().getId());
        Map<Integer, SimpleCEventInfo> infos =
            SessionManager.getAppService().doAction(action).getMap();
        ((CollectionEventAdapter) adapter).setValue(infos.get(savedCeventId));
    }

    @Override
    protected boolean openViewAfterSaving() {
        return true;
    }

    private void savePvInfoValueFromControlType(FormPvCustomInfo pvCustomInfo) {
        // for text and combo, the databinding is used
        if (pvCustomInfo.control instanceof DateTimeWidget) {
            pvCustomInfo.setValue(((DateTimeWidget) pvCustomInfo.control)
                .getText());
        } else if (pvCustomInfo.control instanceof ComboAndQuantityWidget) {
            pvCustomInfo
                .setValue(((ComboAndQuantityWidget) pvCustomInfo.control)
                    .getText());
        } else if (pvCustomInfo.control instanceof SelectMultipleWidget) {
            String[] values =
                ((SelectMultipleWidget) pvCustomInfo.control).getSelections();
            pvCustomInfo.setValue(StringUtils.join(values,
                FormPvCustomInfo.VALUE_MULTIPLE_SEPARATOR));
        }
    }

    @Override
    public String getNextOpenedFormId() {
        return CollectionEventViewForm.ID;
    }

    @Override
    public void setValues() throws Exception {
        GuiUtil.reset(activityStatusComboViewer, cevent.getActivityStatus());
        specimensTable.reload(sourceSpecimens);
        commentEntryTable.setList(ModelWrapper.wrapModelCollection(
            SessionManager.getAppService(), ceventInfo.cevent.getComments(),
            CommentWrapper.class));
        timeDrawnWidget.setDate(new Date());
        resetPvCustomInfo();
    }

    private void resetPvCustomInfo() throws Exception {
        for (FormPvCustomInfo pvCustomInfo : pvCustomInfoList) {
            updateControlForLabel(pvCustomInfo);
        }
    }
}
