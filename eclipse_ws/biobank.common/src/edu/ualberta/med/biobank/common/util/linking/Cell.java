package edu.ualberta.med.biobank.common.util.linking;

import java.io.Serializable;

public class Cell implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer row;

    private Integer col;

    private String value;

    private CellStatus status;

    private String information;

    private String title = "";

    public Cell(int row, int col, String value, CellStatus status) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.status = status;
    }

    public CellStatus getStatus() {
        return status;
    }

    public void setStatus(CellStatus status) {
        this.status = status;
    }

    /**
     * usually displayed in the middle of the cell
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Usually used for the tooltip of the cell
     * 
     * @return
     */
    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static boolean hasValue(Cell cell) {
        return cell != null && cell.getValue() != null;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getRow() {
        return row;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    public Integer getCol() {
        return col;
    }

}
