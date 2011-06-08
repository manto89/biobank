package edu.ualberta.med.biobank.forms;

import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.gui.common.BiobankLogger;
import edu.ualberta.med.biobank.gui.common.widgets.BiobankText;
import edu.ualberta.med.biobank.treeview.SpecimenAdapter;
import edu.ualberta.med.biobank.widgets.grids.ContainerDisplayWidget;
import edu.ualberta.med.biobank.widgets.infotables.DispatchInfoTable;

public class SpecimenViewForm extends BiobankViewForm {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(SpecimenViewForm.class.getName());

    public static final String ID = "edu.ualberta.med.biobank.forms.SpecimenViewForm";

    private SpecimenAdapter specimenAdapter;

    private SpecimenWrapper specimen;

    private BiobankText centerLabel;

    private BiobankText sampleTypeLabel;

    private BiobankText createdDateLabel;

    private BiobankText volumeLabel;

    private BiobankText studyLabel;

    private BiobankText patientLabel;

    private BiobankText activityStatusLabel;

    private BiobankText commentLabel;

    private BiobankText positionLabel;

    private DispatchInfoTable dispatchInfoTable;

    private BiobankText collectionLabel;

    @Override
    public void init() throws Exception {
        Assert.isTrue((adapter instanceof SpecimenAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        specimenAdapter = (SpecimenAdapter) adapter;
        specimen = specimenAdapter.getSpecimen();
        retrieveSpecimen();
        SessionManager.logLookup(specimen);
        setPartName("Specimen: " + specimen.getInventoryId());
    }

    private void retrieveSpecimen() {
        try {
            specimen.reload();
        } catch (Exception e) {
            logger.error("Can't reload specimen with id " + specimen.getId());
        }
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Specimen " + specimen.getInventoryId());
        GridLayout layout = new GridLayout(1, false);
        page.setLayout(layout);
        page.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createInformationSection();
        createDispatchSection();
        createContainersSection();
        setValues();
    }

    private void createDispatchSection() {
        Section section = createSection("Dispatch History");
        Composite client = toolkit.createComposite(section);
        section.setClient(client);
        section.setExpanded(false);
        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dispatchInfoTable = new DispatchInfoTable(client, specimen);
    }

    private void createInformationSection() {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);
        sampleTypeLabel = createReadOnlyLabelledField(client, SWT.NONE, "Type");
        createdDateLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Created");
        volumeLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Volume (ml)");
        studyLabel = createReadOnlyLabelledField(client, SWT.NONE, "Study");
        patientLabel = createReadOnlyLabelledField(client, SWT.NONE, "Patient");
        centerLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Current center");
        positionLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Position");
        collectionLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Collection");
        activityStatusLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Activity Status");
        commentLabel = createReadOnlyLabelledField(client,
            SWT.WRAP | SWT.MULTI, "Comment");

    }

    private void createContainersSection() {
        if (specimen.getParentContainer() != null) {
            Section section = createSection("Container Visualization");
            Composite containersComposite = toolkit.createComposite(section);
            section.setClient(containersComposite);
            section.setExpanded(false);
            containersComposite.setLayout(new GridLayout(1, false));
            toolkit.paintBordersFor(containersComposite);

            Stack<ContainerWrapper> parents = new Stack<ContainerWrapper>();
            ContainerWrapper container = specimen.getParentContainer();
            while (container != null) {
                parents.push(container);
                container = container.getParentContainer();
            }
            while (!parents.isEmpty()) {
                container = parents.pop();
                RowColPos position;
                if (parents.isEmpty()) {
                    position = specimen.getPosition();
                } else {
                    position = parents.peek().getPositionAsRowCol();
                }
                Composite containerComposite = toolkit
                    .createComposite(containersComposite);
                GridLayout layout = new GridLayout(1, false);
                layout.horizontalSpacing = 0;
                layout.marginWidth = 0;
                layout.verticalSpacing = 0;
                containerComposite.setLayout(layout);
                toolkit
                    .createLabel(containerComposite, container.getLabel()
                        + " (" + container.getContainerType().getNameShort()
                        + ") ");
                ContainerDisplayWidget containerWidget = new ContainerDisplayWidget(
                    containerComposite);
                containerWidget.setContainer(container);
                containerWidget.setSelection(position);
                toolkit.adapt(containerWidget);
            }
        }
    }

    private void setValues() {
        setTextValue(centerLabel, specimen.getCurrentCenter().getNameShort());
        setTextValue(sampleTypeLabel, specimen.getSpecimenType().getName());
        setTextValue(createdDateLabel, specimen.getFormattedCreatedAt());
        setTextValue(volumeLabel, specimen.getQuantity() == null ? null
            : specimen.getQuantity().toString());
        setTextValue(studyLabel, specimen.getCollectionEvent().getPatient()
            .getStudy().getNameShort());
        setTextValue(patientLabel, specimen.getCollectionEvent().getPatient()
            .getPnumber());
        setTextValue(positionLabel, specimen.getPositionString(true, false));
        setTextValue(collectionLabel, specimen.getCollectionInfo());
        setTextValue(activityStatusLabel, specimen.getActivityStatus());
        setTextValue(commentLabel, specimen.getComment());

    }

    @Override
    public void setFocus() {
        // specimens are not present in treeviews, unnecessary reloads can be
        // prevented with this method
    }

    @Override
    public void reload() {
        retrieveSpecimen();
        setValues();
        setPartName("Specimen: " + specimen.getInventoryId());
        form.setText("Specimen: " + specimen.getInventoryId());
        dispatchInfoTable.reloadCollection();
    }

}
