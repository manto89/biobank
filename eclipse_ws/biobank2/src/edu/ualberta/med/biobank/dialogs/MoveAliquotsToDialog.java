package edu.ualberta.med.biobank.dialogs;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.validators.AbstractValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;

/**
 * Allows the user to choose a container to which aliquots will be moved
 */
public class MoveAliquotsToDialog extends BiobankDialog {

    private ContainerWrapper oldContainer;

    private IObservableValue newLabel = new WritableValue("", String.class);

    private HashMap<String, ContainerWrapper> map;

    public MoveAliquotsToDialog(Shell parent, ContainerWrapper oldContainer) {
        super(parent);
        Assert.isNotNull(oldContainer);
        this.oldContainer = oldContainer;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        String title = "Move aliquots from one container to another";
        shell.setText(title);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle("Move aliquots from container " + oldContainer.getLabel()
            + " to another");
        setMessage("Select the new container that can hold the aliquots.");
        return contents;
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(1, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        List<SampleTypeWrapper> typesFromOlContainer = oldContainer
            .getContainerType().getSampleTypeCollection();
        List<ContainerWrapper> conts = ContainerWrapper
            .getEmptyContainersHoldingSampleType(
                SessionManager.getAppService(), SessionManager.getInstance()
                    .getCurrentSite(), typesFromOlContainer, oldContainer
                    .getRowCapacity(), oldContainer.getColCapacity());

        map = new HashMap<String, ContainerWrapper>();
        for (ContainerWrapper cont : conts) {
            map.put(cont.getLabel(), cont);
        }
        AbstractValidator validator = new AbstractValidator(
            "Destination container must accepts these aliquots, must be empty and as big as the previous one.") {

            @Override
            public IStatus validate(Object value) {
                if (!(value instanceof String)) {
                    throw new RuntimeException(
                        "Not supposed to be called for non-strings.");
                }

                ContainerWrapper cont = map.get(value);
                if (cont == null) {
                    showDecoration();
                    return ValidationStatus.error(errorMessage);
                } else {
                    hideDecoration();
                    return Status.OK_STATUS;
                }
            }

        };
        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.FILL,
            "New Container Label", null, newLabel, validator);

    }

    public ContainerWrapper getNewContainer() {
        return map.get(newLabel.getValue());
    }

}