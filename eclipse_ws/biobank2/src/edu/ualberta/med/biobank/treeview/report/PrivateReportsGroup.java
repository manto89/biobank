package edu.ualberta.med.biobank.treeview.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ReportWrapper;
import edu.ualberta.med.biobank.model.Report;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class PrivateReportsGroup extends AbstractReportGroup {
    private static final I18n i18n = I18nFactory
        .getI18n(PrivateReportsGroup.class);

    @SuppressWarnings("nls")
    private static final String NODE_NAME = i18n.tr("My Reports");
    @SuppressWarnings("nls")
    private static final String HQL_REPORT_OF_USER = "from "
        + Report.class.getName() + " where userId = ?";

    public PrivateReportsGroup(AdapterBase parent, int id) {
        super(parent, id, NODE_NAME);
    }

    @Override
    protected Collection<ReportWrapper> getReports() {
        List<ReportWrapper> reports = new ArrayList<ReportWrapper>();

        if (SessionManager.getInstance().isConnected()) {
            Integer userId = SessionManager.getUser().getId().intValue();
            HQLCriteria criteria = new HQLCriteria(HQL_REPORT_OF_USER,
                Arrays.asList(new Object[] { userId }));
            try {
                List<Report> rawReports = SessionManager.getAppService().query(
                    criteria);
                for (Report rawReport : rawReports) {
                    reports.add(new ReportWrapper(SessionManager
                        .getAppService(), rawReport));
                }
            } catch (ApplicationException e) {
                e.printStackTrace();
            }
        }

        return reports;
    }

}
