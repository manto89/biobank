package edu.ualberta.med.biobank.server.reports;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.PatientVisit;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class FTAReportImpl extends AbstractReport {

    private static final String QUERY = "aliquots from "
        + PatientVisit.class.getName()
        + " pv join pv.aliquotCollection as aliquots where aliquots.sampleType.nameShort ="
        + FTA_CARD_SAMPLE_TYPE_NAME
        + " and a.patientVisit.patient.study.nameShort = ? and a.patientVisit.patient.study.site "
        + SITE_OPERATOR + SITE_ID
        + " group by pv having min(aliquots.dateProcessed) > ?";

    public FTAReportImpl(BiobankReport report) {
        super(QUERY, report);
    }

    @Override
    protected List<Object> postProcess(WritableApplicationService appService,
        List<Object> results) {
        ArrayList<Object> modifiedResults = new ArrayList<Object>();
        // get the info
        for (Object ob : results) {
            Aliquot a = (Aliquot) ob;
            String pnumber = a.getPatientVisit().getPatient().getPnumber();
            String inventoryId = a.getInventoryId();
            String dateProcessed = DateFormatter.formatAsDate(a
                .getPatientVisit().getDateProcessed());
            String stName = a.getSampleType().getNameShort();
            AliquotWrapper aliquotWrapper = new AliquotWrapper(appService, a);
            String aliquotLabel = aliquotWrapper.getPositionString(true, true);
            modifiedResults.add(new Object[] { pnumber, dateProcessed,
                inventoryId, stName, aliquotLabel });
        }
        return modifiedResults;
    }
}