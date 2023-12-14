package simpledb.query;

import java.util.Arrays;

public class LeapFrogTrieJoinAlgorithmScan implements Scan {

    private Scan outerScan, innerScan;
    // join fields for the outer and inner relations
    private String[] outerJoinFields, innerJoinFields;
    private boolean moreOuter = true, moreInner = true;

    public LeapFrogTrieJoinAlgorithmScan(Scan outerScan, Scan innerScan, String[] outerJoinFields, String[] innerJoinFields) {
        this.outerScan = outerScan;
        this.innerScan = innerScan;
        this.outerJoinFields = outerJoinFields;
        this.innerJoinFields = innerJoinFields;
        // initialize scans to the beginning
        outerScan.beforeFirst();
        innerScan.beforeFirst();
    }

    // reset the scans and flags before the first iteration
    @Override
    public void beforeFirst() {
        outerScan.beforeFirst();
        innerScan.beforeFirst();
        moreOuter = outerScan.next();
        moreInner = innerScan.next();
    }

    // move to the next valid pair of tuples satisfying the join condition
    @Override
    public boolean next() {
        while (moreOuter && moreInner) {
            // retrieve values of the join columns from the outer and inner relations
            String valOuter = outerScan.getString(outerJoinFields[0]);
            String valInner = innerScan.getString(innerJoinFields[0]);

            // check if the values are equal, indicating a match
            if (valOuter.equals(valInner)) {
                return true;
            } else if (valOuter.compareTo(valInner) < 0) {
                // move to the next tuple in the outer relation
                moreOuter = outerScan.next();
                if (!moreOuter) {
                    return false;
                }
            } else {
                // move to the next tuple in the inner relation
                moreInner = innerScan.next();
                if (!moreInner) {
                    return false;
                }
            }
        }
        return false;
    }

    // close scans
    @Override
    public void close() {
        outerScan.close();
        innerScan.close();
    }

    // get the value of a specified field
    @Override
    public Constant getVal(String fldname) {
        if (Arrays.asList(outerJoinFields).contains(fldname)) {
            return outerScan.getVal(fldname);
        } else {
            return innerScan.getVal(fldname);
        }
    }

    // get the integer value of a specified field
    @Override
    public int getInt(String fldname) {
        if (Arrays.asList(outerJoinFields).contains(fldname)) {
            return outerScan.getInt(fldname);
        } else {
            return innerScan.getInt(fldname);
        }
    }

    // get the string value of a specified field
    @Override
    public String getString(String fldname) {
        if (Arrays.asList(outerJoinFields).contains(fldname)) {
            return outerScan.getString(fldname);
        } else {
            return innerScan.getString(fldname);
        }
    }

    // check if a specified field is present
    @Override
    public boolean hasField(String fldname) {
        return Arrays.asList(outerJoinFields).contains(fldname) || Arrays.asList(innerJoinFields).contains(fldname);
    }
}
