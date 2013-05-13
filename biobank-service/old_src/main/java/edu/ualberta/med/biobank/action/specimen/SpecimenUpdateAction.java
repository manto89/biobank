package edu.ualberta.med.biobank.action.specimen;

import edu.ualberta.med.biobank.CommonBundle;
import edu.ualberta.med.biobank.action.Action;
import edu.ualberta.med.biobank.action.ActionContext;
import edu.ualberta.med.biobank.action.EmptyResult;
import edu.ualberta.med.biobank.action.comment.CommentUtil;
import edu.ualberta.med.biobank.action.exception.ActionException;
import edu.ualberta.med.biobank.permission.specimen.SpecimenUpdatePermission;
import edu.ualberta.med.biobank.i18n.Bundle;
import edu.ualberta.med.biobank.i18n.LocalizedException;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.study.CollectionEvent;
import edu.ualberta.med.biobank.model.study.Specimen;
import edu.ualberta.med.biobank.model.type.ActivityStatus;

public class SpecimenUpdateAction implements Action<EmptyResult> {
    private static final long serialVersionUID = 1L;
    private static final Bundle bundle = new CommonBundle();

    private Integer specimenId;
    private Integer specimenTypeId;
    private Integer collectionEventId;
    private ActivityStatus activityStatus;
    private String commentMessage;
    private Integer parentSpecimenId;

    public void setSpecimenId(Integer specimenId) {
        this.specimenId = specimenId;
    }

    public void setSpecimenTypeId(Integer specimenTypeId) {
        this.specimenTypeId = specimenTypeId;
    }

    public void setCollectionEventId(Integer collectionEventId) {
        this.collectionEventId = collectionEventId;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    public void setCommentMessage(String commentMessage) {
        this.commentMessage = commentMessage;
    }

    public void setParentSpecimenId(Integer parentSpecimenId) {
        this.parentSpecimenId = parentSpecimenId;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new SpecimenUpdatePermission(specimenId).isAllowed(context);
    }

    @Override
    public EmptyResult run(ActionContext context) throws ActionException {
        Specimen specimen = context.load(Specimen.class, specimenId);

        SpecimenType specimenType =
            context.load(SpecimenType.class, specimenTypeId);
        specimen.setSpecimenType(specimenType);
        // get is intended, load is wrong
        specimen.setParentSpecimen(context.get(Specimen.class,
            parentSpecimenId));
        specimen.setActivityStatus(activityStatus);

        addComment(context, specimen);

        updateCollectionEvent(context, specimen);
        updateTopSpecimen(context, specimen);

        return new EmptyResult();
    }

    private void updateTopSpecimen(ActionContext context, Specimen specimen) {
        if (specimen.getParentSpecimen() == null)
            specimen.setTopSpecimen(specimen);
        else
            specimen.setTopSpecimen(specimen.getParentSpecimen()
                .getTopSpecimen());
        context.getSession().saveOrUpdate(specimen);
        for (Specimen spec : specimen.getChildSpecimens())
            updateTopSpecimen(context, spec);
    }

    private Comment addComment(ActionContext context, Specimen specimen) {
        Comment comment = CommentUtil.create(context.getUser(), commentMessage);
        if (comment != null) {
            context.getSession().save(comment);
            specimen.getComments().add(comment);
        }
        return comment;
    }

    @SuppressWarnings("nls")
    private void updateCollectionEvent(ActionContext context,
        Specimen specimen) {
        // when i came across this old and new were reversed... definitely
        // wrong. Test prolly breaks now if it ever worked
        CollectionEvent oldCEvent = specimen.getCollectionEvent();
        CollectionEvent newCEvent =
            context.load(CollectionEvent.class, collectionEventId);

        specimen.setCollectionEvent(newCEvent);
        if (specimen.getParentSpecimen() == null)
            specimen.setOriginalCollectionEvent(newCEvent);
        else {
            specimen.setOriginalCollectionEvent(null);
            if (specimen.getParentSpecimen()
                .getProcessingEvent() == null)
                throw new LocalizedException(
                    bundle
                        .tr("You must select a parent with a processing event")
                        .format());
        }
        context.getSession().saveOrUpdate(specimen);
        if (!oldCEvent.equals(newCEvent)) {
            updateChildSpecimensCEvent(context, specimen, newCEvent);
        }

    }

    private void updateChildSpecimensCEvent(ActionContext context,
        Specimen specimen,
        CollectionEvent cEvent) {
        for (Specimen childSpecimen : specimen.getChildSpecimens()) {
            childSpecimen.setCollectionEvent(cEvent);
            context.getSession().saveOrUpdate(childSpecimen);
            updateChildSpecimensCEvent(context, childSpecimen, cEvent);
        }
    }

}