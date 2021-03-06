package se.andelain.dcc;

import javax.swing.table.AbstractTableModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Table Model for presenting FbBit objects in a JTable.
 */
public class FbBitTableModel extends AbstractTableModel implements Serializable{

    private List<FbBit> fbBits;

    public FbBitTableModel(List<FbBit> fbBits){
        this.fbBits = new ArrayList<FbBit>(fbBits);
    }

    public FbBitTableModel(){
        this.fbBits = new ArrayList<FbBit>();
    }

    public List<FbBit> getFbBits(){
        return fbBits;
    }

    public void setFbBits(List<FbBit> fbBits){
        this.fbBits = fbBits;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return fbBits.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = "unknown";
        //System.out.println("Attempted to get row: " + rowIndex + " column: "+ columnIndex);
        //System.out.println("fBbits.size: "+fbBits.size());
        FbBit fbBit = fbBits.get(rowIndex);
        switch(columnIndex){
            case 0:
                value = fbBit.getBusName();
                break;
            case 1:
                value = fbBit.getAddr();
                break;
            case 2:
                value = fbBit.getBit();
                break;
            case 3:
                value = fbBit.getName();
                break;
            case 4:
                value = fbBit.getStatus();
                break;
            case 5:
                value = fbBit.getChanges();
                break;
        }

        return value;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }


    /**
     * Allow user to edit custom name of FbBit objects
     * @param row
     * @param col
     * @return
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 3) {
            return true;
        } else {
            return false;
        }
    }

    public void updateFbBit(String busName, int addr, int bit, boolean status){
        //Check if it already exists!
        for(FbBit fb : fbBits){
            if(fb.getBusName().compareTo(busName) == 0 && fb.getAddr() == addr && fb.getBit() == bit){
                fb.setStatus(status);
                fireTableCellUpdated(fbBits.indexOf(fb),3);
                fireTableCellUpdated(fbBits.indexOf(fb),4);
                return;
            }
        }
        //If it didn't exist, add it
        System.out.println("Addr: "+addr);
        addFbBit(new FbBit(busName, addr,bit,status));
    }
    public void addFbBit(FbBit fbBit) {
        fbBits.add(fbBit);
        System.out.println("RowCount: "+getRowCount());
        fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
    }

    public void setValueAt(Object value, int row, int col) {
        if(col == 3 && value.getClass() == String.class) {
            fbBits.get(row).setName((String) value);
            fireTableCellUpdated(row, col);
        }

    }

    public String getColumnName(int column) {
        switch (column){
            case 0:
                return "Bus";
            case 1:
                return "Addr";
            case 2:
                return "Bit";
            case 3:
                return "Name";
            case 4:
                return "Status";
            case 5:
                return "Changes";
            default:
                return null;
        }
    }

}
