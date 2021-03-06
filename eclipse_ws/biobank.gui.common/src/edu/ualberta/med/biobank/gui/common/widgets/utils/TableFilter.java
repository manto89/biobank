package edu.ualberta.med.biobank.gui.common.widgets.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public abstract class TableFilter<T> {
    private static final I18n i18n = I18nFactory.getI18n(TableFilter.class);

    @SuppressWarnings("nls")
    private static final String TABLE_FILTER_DESCRIPTION = i18n
        .tr("Enter text to filter the list:");

    private final Text filterText;

    public TableFilter(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(TABLE_FILTER_DESCRIPTION);
        filterText = new Text(parent, SWT.BORDER);
        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        filterText.setLayoutData(gd);
        filterText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                List<T> filteredObjects = null;
                String text = filterText.getText();
                if (text.isEmpty())
                    filteredObjects = getAllCollection();
                else {
                    filteredObjects = new ArrayList<T>();
                    for (T object : getAllCollection()) {
                        if (accept(object, text))
                            filteredObjects.add(object);
                    }
                }
                setFilteredList(filteredObjects);
            }
        });
    }

    protected abstract boolean accept(T object, String text);

    public abstract List<T> getAllCollection();

    public abstract void setFilteredList(List<T> filteredObjects);

    public static boolean contains(String candidate, String searchedText) {
        if (candidate == null)
            return false;
        return candidate.toLowerCase().contains(searchedText.toLowerCase());
    }

}
