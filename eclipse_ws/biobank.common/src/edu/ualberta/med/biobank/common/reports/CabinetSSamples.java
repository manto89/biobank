package edu.ualberta.med.biobank.common.reports;

import java.util.List;

import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.ContainerPath;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class CabinetSSamples extends QueryObject {

    protected static final String NAME = "Cabinet Aliquots per Study";

    public CabinetSSamples(String op, Integer siteId) {
        super(
            "Displays the total number of cabinet aliquots per study.",

            "select aliquot.patientVisit.patient.study.nameShort, count(*) from "
                + Aliquot.class.getName()
                + " as aliquot where aliquot.aliquotPosition.container.id in (select path1.container.id from "
                + ContainerPath.class.getName()
                + " as path1, "
                + ContainerPath.class.getName()
                + " as path2 where locate(path2.path, path1.path) > 0 and path2.container.containerType.name like ?) and aliquot.patientVisit.patient.study.site"
                + op + siteId
                + " group by aliquot.patientVisit.patient.study.nameShort",
            new String[] { "Study", "Total" });
    }

    @Override
    public List<Object> executeQuery(WritableApplicationService appService,
        List<Object> params) throws ApplicationException {
        params.add("%Cabinet%");
        HQLCriteria c = new HQLCriteria(queryString);
        c.setParameters(params);
        return appService.query(c);
    }

    @Override
    public String getName() {
        return NAME;
    }
}