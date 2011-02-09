package edu.ualberta.med.biobank.common.util;

/**
 * Never remove one of these enum. Use deprecated if it should not be used
 * anymore.
 */
public enum DispatchItemState {
    NONE(0, "Ok"), RECEIVED(1, "Received"), MISSING(2, "Missing"), EXTRA(3,
        "Extra");

    private Integer id;
    private String label;

    private DispatchItemState(Integer id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public boolean isEquals(Integer state) {
        return id.equals(state);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public static DispatchItemState getState(Integer state) {
        for (DispatchItemState das : values()) {
            if (das.isEquals(state)) {
                return das;
            }
        }
        return null;
    }
}