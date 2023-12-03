package simpledb.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeapFrogTrieJoinAlgorithmScan implements Scan {
    private Scan s1, s2;
    private String s1Col, s2Col;
    private List<IntConstant> s1Values, s2Values;
    private int currentS1Index, currentS2Index;

    public LeapFrogTrieJoinAlgorithmScan(Scan s1, Scan s2, String s1Col, String s2Col) {
        this.s1 = s1;
        this.s2 = s2;
        this.s1Col = s1Col;
        this.s2Col = s2Col;
        this.s1Values = new ArrayList<>();
        this.s2Values = new ArrayList<>();
        this.currentS1Index = -1;
        this.currentS2Index = -1;
    }

    @Override
    public void beforeFirst() {
        s1.beforeFirst();
        s2.beforeFirst();
        s1Values.clear();
        s2Values.clear();
        currentS1Index = -1;
        currentS2Index = -1;
    }

    @Override
    public boolean next() {
        if (s2.next()) {
            IntConstant leftColVal = (IntConstant) getVal(s1Col);
            IntConstant rightColVal = (IntConstant) getVal(s2Col);

            if (leftColVal.equals(rightColVal)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (currentS1Index == -1) {
                initializeValues();
            } else {
                currentS1Index++;
            }

            while (currentS1Index < s1Values.size()) {
                s1.beforeFirst();
                s2.beforeFirst();

                Object targetValue = s1Values.get(currentS1Index).asJavaVal();
                if (moveToValue(targetValue, s2Values)) {
                    return true;
                }

                currentS1Index++;
            }
            return false;
        }
    }

    @Override
    public void close() {
        s1.close();
        s2.close();
    }

    @Override
    public Constant getVal(String fldname) {
        if (s1.hasField(fldname)) {
            return s1.getVal(fldname);
        } else if(s2.hasField(fldname)){
            return s2.getVal(fldname);
        }
        
        return null;
    }

    @Override
    public int getInt(String fldname) {
        if (hasField(fldname)) {
            return s1.getInt(fldname);
        } else {
            return s2.getInt(fldname);
        }
    }

    @Override
    public String getString(String fldname) {
        if (hasField(fldname)) {
            return s1.getString(fldname);
        } else {
            return s2.getString(fldname);
        }
    }

    @Override
    public boolean hasField(String fldname) {
        return s1.hasField(fldname) || s2.hasField(fldname);
    }

    private void initializeValues() {
        while (s1.next()) {
            s1Values.add((IntConstant) getVal(s1Col));
        }
        Collections.sort(s1Values);

        while (s2.next()) {
            s2Values.add((IntConstant) getVal(s2Col));
        }
        Collections.sort(s2Values);
    }

    private boolean moveToValue(Object target, List<IntConstant> values) {
        int lo = 0;
        int hi = values.size() - 1;

        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int midValue = (int) values.get(mid).asJavaVal();

            if (midValue == (int)target) {
                currentS2Index = mid;
                return true;
            } else if (midValue < (int)target) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }

        return false;
    }
}
