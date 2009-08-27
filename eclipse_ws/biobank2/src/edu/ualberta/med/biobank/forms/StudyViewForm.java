package edu.ualberta.med.biobank.forms;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.utils.ModelUtils;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.PvInfo;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.SiteAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;
import edu.ualberta.med.biobank.widgets.infotables.SampleSourceInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.SampleStorageInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.StudyContactInfoTable;

public class StudyViewForm extends BiobankViewForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.StudyViewForm";

    private StudyAdapter studyAdapter;
    private Study study;

    private Label nameShortLabel;
    private Label activityStatusLabel;
    private Label commentLabel;

    private StudyContactInfoTable clinicsTable;
    private SampleStorageInfoTable sampleStorageTable;
    private SampleSourceInfoTable sampleSourceTable;

    // private BiobankCollectionTable pvInfosTable;

    @Override
    public void init() {
        Assert.isTrue((adapter instanceof StudyAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        studyAdapter = (StudyAdapter) adapter;

        // retrieve info from database because could have been modified
        // after first opening
        retrieveStudy();
        setPartName("Study " + study.getNameShort());
    }

    @Override
    protected void createFormContent() throws Exception {
        if (study.getName() != null) {
            form.setText("Study: " + study.getName());
        }

        addRefreshToolbarAction();

        GridLayout layout = new GridLayout(1, false);
        form.getBody().setLayout(layout);
        form.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite client = toolkit.createComposite(form.getBody());
        client.setLayout(new GridLayout(2, false));
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        nameShortLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Short Name");
        activityStatusLabel = (Label) createWidget(client, Label.class,
            SWT.NONE, "Activity Status");
        commentLabel = (Label) createWidget(client, Label.class, SWT.WRAP,
            "Comments");

        setStudySectionValues();
        createClinicSection();
        createSampleStorageSection();
        createSampleSourceSection();
        createPvDataSection();

        final Button edit = toolkit.createButton(client,
            "Edit this information", SWT.PUSH);
        edit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getSite().getPage().closeEditor(StudyViewForm.this, false);
                try {
                    getSite().getPage().openEditor(new FormInput(studyAdapter),
                        StudyEntryForm.ID, true);
                } catch (PartInitException exp) {
                    exp.printStackTrace();
                }
            }
        });
    }

    private void createClinicSection() {
        Composite client = createSectionWithClient("Clinics");

        clinicsTable = new StudyContactInfoTable(client, appService, study);
        clinicsTable.adaptToToolkit(toolkit, true);
        toolkit.paintBordersFor(clinicsTable);

        clinicsTable.getTableViewer().addDoubleClickListener(
            FormUtils.getBiobankCollectionDoubleClickListener());
    }

    private void setStudySectionValues() {
        FormUtils.setTextValue(nameShortLabel, study.getNameShort());
        FormUtils.setTextValue(activityStatusLabel, study.getActivityStatus());
        FormUtils.setTextValue(commentLabel, study.getComment());
    }

    private void createSampleStorageSection() {
        Section section = createSection("Sample Storage");

        sampleStorageTable = new SampleStorageInfoTable(section, study
            .getSampleStorageCollection());
        section.setClient(sampleStorageTable);
        sampleStorageTable.adaptToToolkit(toolkit, true);
        toolkit.paintBordersFor(sampleStorageTable);
    }

    private void createSampleSourceSection() {
        Section section = createSection("Source Vessels");
        sampleSourceTable = new SampleSourceInfoTable(section, study
            .getSampleSourceCollection());
        section.setClient(sampleSourceTable);
        sampleStorageTable.adaptToToolkit(toolkit, true);
        toolkit.paintBordersFor(sampleStorageTable);
    }

    private void createPvDataSection() {
        Section section = createSection("Patient Visit Information Collected");

        // FIXME this information can be displayed in a better way
        //
        // String[] headings = new String[] { "Name", "Valid Values (optional)"
        // };
        // pvInfosTable = new BiobankCollectionTable(section, SWT.NONE,
        // headings,
        // getStudyPvInfo());
        // section.setClient(pvInfosTable);
        // pvInfosTable.adaptToToolkit(toolkit, true);
        // toolkit.paintBordersFor(pvInfosTable);
        //
        // pvInfosTable.getTableViewer().addDoubleClickListener(
        // FormUtils.getBiobankCollectionDoubleClickListener());
    }

    private PvInfo[] getStudyPvInfo() {
        // hack required here because study.getXxxCollection().toArray(new
        // Xxx[0]) returns Object[].
        Collection<PvInfo> pvInfos = study.getPvInfoCollection();
        if ((pvInfos == null) || (pvInfos.size() == 0))
            return null;
        int count = 0;
        if (pvInfos == null)
            return null;

        PvInfo[] arr = new PvInfo[pvInfos.size()];
        for (PvInfo p : pvInfos) {
            arr[count] = p;
            ++count;
        }
        return arr;
    }

    @Override
    protected void reload() throws Exception {
        retrieveStudy();
        setPartName("Study " + study.getNameShort());
        form.setText("Study: " + study.getName());
        setStudySectionValues();
        AdapterBase clinicGroupNode = ((SiteAdapter) studyAdapter.getParent()
            .getParent()).getClinicGroupNode();
        clinicsTable.getTableViewer().setInput(
            FormUtils.getClinicsAdapters(clinicGroupNode, ModelUtils
                .getStudyClinicCollection(appService, study)));
        // pvInfosTable.getTableViewer().setInput(getStudyPvInfo());
    }

    private void retrieveStudy() {
        try {
            study = ModelUtils.getObjectWithId(studyAdapter.getAppService(),
                Study.class, studyAdapter.getStudy().getId());
            studyAdapter.setStudy(study);
        } catch (Exception e) {
            SessionManager.getLogger().error(
                "Error while retrieving study "
                    + studyAdapter.getStudy().getName(), e);
        }
    }
}
