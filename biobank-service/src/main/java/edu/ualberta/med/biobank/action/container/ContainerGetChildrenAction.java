package edu.ualberta.med.biobank.action.container;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.biobank.action.Action;
import edu.ualberta.med.biobank.action.ActionContext;
import edu.ualberta.med.biobank.action.ListResult;
import edu.ualberta.med.biobank.action.exception.ActionException;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.center.Container;
import edu.ualberta.med.biobank.model.center.ContainerPosition;
import edu.ualberta.med.biobank.model.center.ContainerType;
import edu.ualberta.med.biobank.permission.container.ContainerReadPermission;

public class ContainerGetChildrenAction implements
    Action<ListResult<Container>> {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory
        .getLogger(ContainerGetInfoAction.class.getName());

    private final Integer parentContainerId;

    public ContainerGetChildrenAction(Integer parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        Container c = context.load(Container.class, parentContainerId);
        return new ContainerReadPermission(c.getSite().getId())
            .isAllowed(context);
    }

    @SuppressWarnings("nls")
    @Override
    public ListResult<Container> run(ActionContext context)
        throws ActionException {
        log.info("run: parentContainerId={}", parentContainerId);

        Container parentContainer =
            context.load(Container.class, parentContainerId);
        ArrayList<Container> childContainers =
            new ArrayList<Container>(parentContainer.getChildPositions().size());

        parentContainer.getContainerType().getChildLabelingScheme().getName();

        // used to NOT iterate over the same child container types
        HashSet<ContainerType> ctSet = new HashSet<ContainerType>();

        for (ContainerPosition pos : parentContainer.getChildPositions()) {
            Container child = pos.getContainer();
            childContainers.add(child);

            child.getSite().getName();

            log.debug("run: parentContainerId={} getting container types");

            // need to initialize containerType.childContainerTypes to
            // support container drag and drop.
            for (ContainerType ct : child.getContainerType()
                .getChildContainerTypes()) {
                ct.getName();

                if (!ctSet.contains(ct)) {
                    ctSet.add(ct);

                    log.info("run: parentContainerId={} childCtype={}",
                        parentContainerId, ct.getName());

                    ct.getCapacity().getRowCapacity();

                    for (SpecimenType st : ct.getSpecimenTypes()) {
                        st.getName();
                    }
                }
            }

            log.debug("run: parentContainerId={} getting specimen positions");

            // specimenPosition set has to be initialized due to the
            // tree adapter needing to know this to display additional menu
            // selections when a right click is done on a container node.
            child.getSpecimenPositions().size();
        }

        log.debug("run: parentContainerId={} exit", parentContainerId);

        return new ListResult<Container>(childContainers);
    }
}