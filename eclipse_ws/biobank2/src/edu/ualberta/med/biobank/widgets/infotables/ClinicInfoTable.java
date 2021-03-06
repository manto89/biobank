package edu.ualberta.med.biobank.widgets.infotables;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.formatters.NumberFormatter;
import edu.ualberta.med.biobank.common.permission.clinic.ClinicDeletePermission;
import edu.ualberta.med.biobank.common.permission.clinic.ClinicReadPermission;
import edu.ualberta.med.biobank.common.permission.clinic.ClinicUpdatePermission;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.gui.common.widgets.AbstractInfoTableWidget;
import edu.ualberta.med.biobank.gui.common.widgets.BgcLabelProvider;
import edu.ualberta.med.biobank.model.HasName;
import edu.ualberta.med.biobank.model.HasNameShort;
import edu.ualberta.med.biobank.model.Patient;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ClinicInfoTable extends InfoTableWidget<ClinicWrapper> {
    public static final I18n i18n = I18nFactory
        .getI18n(ClinicInfoTable.class);

    private static class TableRowData {
        public ClinicWrapper clinic;
        public String clinicName;
        public String clinicNameShort;
        public Integer studyCount;
        public String status;
        public Long patientCount;
        public Long visitCount;

        @SuppressWarnings("nls")
        @Override
        public String toString() {
            return StringUtils.join(new String[] {
                clinicName,
                clinicNameShort,
                studyCount.toString(),
                (status != null) ? status : StringUtil.EMPTY_STRING,
                (patientCount != null) ? patientCount.toString()
                    : StringUtil.EMPTY_STRING,
                (visitCount != null) ? visitCount.toString()
                    : StringUtil.EMPTY_STRING }, "\t");
        }
    }

    @SuppressWarnings("nls")
    private static final String[] HEADINGS = new String[] {
        HasName.PropertyName.NAME.toString(),
        HasNameShort.PropertyName.NAME_SHORT.toString(),
        Patient.NAME.plural().toString(),
        i18n.tr("Status"),
        i18n.tr("Study Count"),
        i18n.tr("Patient Visits") };

    public ClinicInfoTable(Composite parent, List<ClinicWrapper> collection) {
        super(parent, collection, HEADINGS, 10, ClinicWrapper.class);
    }

    @Override
    protected BgcLabelProvider getLabelProvider() {
        return new BgcLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                TableRowData item =
                    (TableRowData) ((BiobankCollectionModel) element).o;
                if (item == null) {
                    if (columnIndex == 0) {
                        return AbstractInfoTableWidget.LOADING;
                    }
                    return StringUtil.EMPTY_STRING;
                }
                switch (columnIndex) {
                case 0:
                    return item.clinicName;
                case 1:
                    return item.clinicNameShort;
                case 2:
                    return NumberFormatter.format(item.studyCount);
                case 3:
                    return item.status;
                case 4:
                    return NumberFormatter.format(item.patientCount);
                case 5:
                    return NumberFormatter.format(item.visitCount);
                default:
                    return StringUtil.EMPTY_STRING;
                }
            }
        };
    }

    @Override
    public Object getCollectionModelObject(Object obj) throws Exception {
        TableRowData info = new TableRowData();
        info.clinic = (ClinicWrapper) obj;
        info.clinicName = info.clinic.getName();
        info.clinicNameShort = info.clinic.getNameShort();
        List<StudyWrapper> studies = info.clinic.getStudyCollection();
        if (studies == null) {
            info.studyCount = 0;
        } else {
            info.studyCount = studies.size();
        }
        info.status = info.clinic.getActivityStatus().getName();
        info.patientCount = info.clinic.getPatientCount();
        info.visitCount = info.clinic.getCollectionEventCount();
        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public ClinicWrapper getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        TableRowData row = (TableRowData) item.o;
        Assert.isNotNull(row);
        return row.clinic;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }

    @Override
    protected Boolean canEdit(ClinicWrapper target) throws ApplicationException {
        return SessionManager.getAppService().isAllowed(
            new ClinicUpdatePermission(target.getId()));
    }

    @Override
    protected Boolean canDelete(ClinicWrapper target)
        throws ApplicationException {
        return SessionManager.getAppService().isAllowed(
            new ClinicDeletePermission(target.getId()));
    }

    @Override
    protected Boolean canView(ClinicWrapper target) throws ApplicationException {
        return SessionManager.getAppService().isAllowed(
            new ClinicReadPermission(target.getId()));
    }

}
