package edu.ualberta.med.biobank.forms.reports;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

public class SpecimenTypeSUsageEditor extends ReportsEditor {

    public static String ID = "edu.ualberta.med.biobank.editors.SampleTypeSUsageEditor"; //$NON-NLS-1$

    @Override
    protected void createOptionSection(Composite parent) {
    }

    @Override
    protected void initReport() {
    }

    @Override
    protected String[] getColumnNames() {
        return new String[] { Messages.SpecimenTypeSUsageEditor_specType_label, Messages.SpecimenTypeSUsageEditor_study_label };
    }

    @Override
    protected List<String> getParamNames() {
        return new ArrayList<String>();
    }

    @Override
    protected List<Object> getPrintParams() throws Exception {
        return new ArrayList<Object>();
    }
}
