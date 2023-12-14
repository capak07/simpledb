package simpledb;

import java.util.*;

import simpledb.metadata.MetadataMgr;
import simpledb.query.*;
import simpledb.record.TableInfo;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;

public class Entites {
    public static void main(String args[]) {
        // initialize SimpleDB
        SimpleDB.init("studentdb");
        
        // start a transaction
        Transaction tx = new Transaction();
        
        // access the metadata manager
        MetadataMgr mdMgr = SimpleDB.mdMgr();

        // get TableInfo and create TableScan for student relation
        TableInfo sti = mdMgr.getTableInfo("student", tx);
        Scan s1 = new TableScan(sti, tx);

        // get TableInfo and create TableScan for course relation
        TableInfo cti = mdMgr.getTableInfo("course", tx);
        Scan s2 = new TableScan(cti, tx);

        // perform Leapfrog Triejoin between student and course on MajorId=DeptId
        Scan s3 = new LeapFrogTrieJoinAlgorithmScan(s1, s2, new String[] { "majorid" }, new String[] { "deptid" });

        // create a selection predicate for the section
        Predicate pred3 = new Predicate(new Term(new FieldNameExpression("cid"), new FieldNameExpression("courseid")));

        // combine results with the section scan using a ProductScan
        Scan s4 = new ProductScan(s3, s2);
        Scan s5 = new SelectScan(s4, pred3);

        // create selection predicates for the query
        Predicate pred1 = new Predicate(new Term(new FieldNameExpression("majorid"), new FieldNameExpression("deptid")));
        Predicate pred2 = new Predicate(new Term(new FieldNameExpression("cid"), new FieldNameExpression("courseid")));

        // apply selection predicates
        Scan s6 = new SelectScan(s5, pred1);
        Scan s7 = new SelectScan(s6, pred2);

        // project the final result
        Collection<String> fieldlist = Arrays.asList("sname", "title", "prof");
        Scan sTop = new ProjectScan(s7, fieldlist);

        // execute the final scan
        sTop.beforeFirst(); // ensure the scan is positioned before the first record
        while (sTop.next()) {
            System.out.println(sTop.getString("sname") + "," + sTop.getString("title") + "," + sTop.getString("prof"));
        }
        sTop.close();
        tx.commit();
    }
}
