package se.andelain.dcc;


import javax.swing.table.AbstractTableModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XpressNetListenerTableModel extends AbstractTableModel implements Serializable {

    private String[] columnNames;
    private List<Object[]> data;

    public XpressNetListenerTableModel(){

        this.data = new ArrayList<Object[]>();
        this.columnNames = new String[]{"Bus name","IP", "Enabled", "Updates/s"};
    }

    public void addBusListener(String busName, String ipAddr, boolean enabled, String updates){
        //Check so that the ip addr is unique
        for(Object[] arr : data){
            if(ipAddr.compareTo((String) arr[1]) == 0){
                //IP is already in use
                //TODO: display error msg
                return;
            }
        }
        data.add(new Object[]{busName,ipAddr,enabled,updates});
        fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
    }

    public void updateBusListenerUpdateData(String busName, int updates){
        //Check if it exists
        for(Object[] arr : data){
            if(busName.compareTo((String)arr[0]) == 0){
                arr[3] = updates;
                fireTableCellUpdated(data.indexOf(arr),3);
                return;
            }
        }
        System.out.println("ERROR: Someone tried to update bus data for non existing bus: "+busName);

    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object[] arr = data.get(rowIndex);
        return arr[columnIndex];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        //Allow user to enable/disable bus
        if (col == 2) {
            return true;
        } else {
            return false;
        }
    }

    public void setValueAt(Object value, int row, int col){

        if((col == 2 && value.getClass() == Boolean.class) || (col == 3 && value.getClass() == String.class)) {
            (data.get(row))[col] = value;
            fireTableCellUpdated(row, col);
        }

    }
}
