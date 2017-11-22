package se.andelain.dcc;

import java.io.Serializable;

public class FbBit implements Serializable{
    private int addr;
    private int bit;
    private boolean status;
    private String name;
    /**
     * Contains the number of status changes that have been logged to this FbBit object.
     */
    private long changes;

    /**
     * Creates a feedback bit information object.
     * A feedback bit always has an address, a bit number and a status.
     * The combination of address and bit number should always be unique to a single FbBit object.
     * If the bit FbBit status is unknown it is recommended to set it to 0/false for consistency.
     * @param addr
     * @param bit
     * @param status
     */
    public FbBit(int addr, int bit, boolean status){
        this.addr = addr;
        this.bit = bit;
        this.status = status;
        name = "none";
        changes = 0;
    }

    public void resetChangeCounter(){
        changes = 0;
    }

    public long getChanges(){
        return changes;
    }

    public boolean getStatus() {
        return status;
    }

    /**
     * Sets the bit status for this FbBit object.
     * Since we may receive info for this bit even though it has not changed (since DCC sends info for
     * four bits even if just one of them changed) we check if the reported status differs from the old one first.
     * If it differs the status is updated and the change counter increased.
     * @param status
     */
    public void setStatus(boolean status) {
        if(this.status != status) {
            this.status = status;
            changes++;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddr() {
        return addr;
    }

    public int getBit() {
        return bit;
    }
}
