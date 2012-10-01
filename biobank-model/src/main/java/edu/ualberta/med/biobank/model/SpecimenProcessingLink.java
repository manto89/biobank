package edu.ualberta.med.biobank.model;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.hibernate.envers.Audited;

import edu.ualberta.med.biobank.model.type.Decimal;

/**
 * A record of the actual {@link Specimen}s and amounts involved in a
 * {@link SpecimenProcessingLinkType}.
 * <p>
 * This entity provides more detailed information about the parentage of a
 * {@link Specimen}, i.e. the {@link #input}-{@link #output} pair could be
 * considered a parent-child relationship. This is opposed to
 * {@link CollectionEvent}s, which provide much more general heritage
 * information. So, special care must be taken to ensure that
 * {@link SpecimenCollectionEvent} and {@link SpecimenProcessingLink} entities
 * are consistent. The {@link #output} must be in all the same
 * {@link CollectionEvent}s as the {@link #input}, but if two {@link Specimen}s
 * are in the same {@link CollectionEvent} they do <em>not</em> need to be
 * associated (directly or transitively) through a
 * {@link SpecimenProcessingLink}. Also note that the {@link #input} does
 * <em>not</em> need to be in the same {@link CollectionEvent}(s) as the
 * {@link #output}.
 * 
 * @author Jonathan Ferland
 * @see SpecimenCollectionEvent
 */
@Audited
@Entity
@Table(name = "SPECIMEN_PROCESSING_LINK")
public class SpecimenProcessingLink
    extends AbstractVersionedModel {
    private static final long serialVersionUID = 1L;

    // TODO: check that input and output have the same processingEvent?
    private SpecimenProcessingEvent input;
    private SpecimenProcessingEvent output;
    private SpecimenProcessingLinkType type;
    private Date timeDone;
    private Decimal actualInputAmountChange;
    private Decimal actualOutputAmountChange;

    /**
     * @return the {@link Specimen} that was processed.
     */
    @NotNull(message = "{SpecimenProcessingLink.input.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INPUT_SPECIMEN_PROCESSING_EVENT_ID", nullable = false)
    public SpecimenProcessingEvent getInput() {
        return input;
    }

    public void setInput(SpecimenProcessingEvent input) {
        this.input = input;
    }

    /**
     * @return the {@link Specimen} that resulted from the process.
     */
    @NotNull(message = "{SpecimenProcessingLink.output.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTPUT_SPECIMEN_PROCESSING_EVENT_ID", nullable = false)
    public SpecimenProcessingEvent getOutput() {
        return output;
    }

    public void setOutput(SpecimenProcessingEvent output) {
        this.output = output;
    }

    /**
     * @return the type of processing that the involved {@link Specimen}s
     *         underwent, or null if unspecified.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECIMEN_PROCESSING_LINK_TYPE_ID", nullable = false)
    public SpecimenProcessingLinkType getType() {
        return type;
    }

    public void setType(SpecimenProcessingLinkType type) {
        this.type = type;
    }

    /**
     * @return when the processing happened, or null if unspecified.
     */
    @Past
    @Column(name = "TIME_DONE")
    public Date getTimeDone() {
        return timeDone;
    }

    public void setTimeDone(Date timeDone) {
        this.timeDone = timeDone;
    }

    /**
     * @return the actual amount removed from the {@link #input}.
     */
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "ACTUAL_INPUT_CHANGE_VALUE")),
        @AttributeOverride(name = "scale", column = @Column(name = "ACTUAL_INPUT_CHANGE_SCALE"))
    })
    public Decimal getActualInputAmountChange() {
        return actualInputAmountChange;
    }

    public void setActualInputAmountChange(Decimal actualInputAmountChange) {
        this.actualInputAmountChange = actualInputAmountChange;
    }

    /**
     * @return the actual amount added to the {@link #output}.
     */
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "ACTUAL_OUTPUT_CHANGE_VALUE")),
        @AttributeOverride(name = "scale", column = @Column(name = "ACTUAL_OUTPUT_CHANGE_SCALE"))
    })
    public Decimal getActualOutputAmountChange() {
        return actualOutputAmountChange;
    }

    public void setActualOutputAmountChange(Decimal actualOutputAmountChange) {
        this.actualOutputAmountChange = actualOutputAmountChange;
    }
}