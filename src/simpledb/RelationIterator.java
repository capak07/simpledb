package src.simpledb;

import java.util.Iterator;

public interface RelationIterator<Integer> extends Iterator, Comparable<RelationIterator<Integer>> {
    int key();

    void seek(int seekKey);

    boolean atEnd();

    void open();

    void up();

    String debugString();
}
