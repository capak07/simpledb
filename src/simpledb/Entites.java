package simpledb;

import java.util.*;

import simpledb.metadata.MetadataMgr;
import simpledb.query.*;
import simpledb.record.TableInfo;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;

public class Entites {
    public static void main(String args[]) {
        SimpleDB.init("studentdb");
        Transaction tx = new Transaction();
        MetadataMgr mdMgr = SimpleDB.mdMgr();

        TableInfo dti = mdMgr.getTableInfo("DEPT", tx);
        Scan s1 = new TableScan(dti, tx);
        TableInfo cti = mdMgr.getTableInfo("COURSE", tx);
        Scan s2 = new TableScan(cti, tx);

        // Leapfrog triejoin between dept and course on DId=DeptId
        Scan s3 = new LeapfrogTriejoinScan(s1, s2, new String[] { "DId" }, new String[] { "DeptId" });

        // Selection predicate for section
        Predicate pred3 = new Predicate(new Term(new FieldNameExpression("CId"), new FieldNameExpression("CourseId")));

        // Combine the results with the section scan
        Scan s4 = new ProductScan(s3, s2);
        Scan s5 = new SelectScan(s4, pred3);

        // Selection predicates for the query
        Predicate pred1 = new Predicate(new Term(new FieldNameExpression("DId"), new FieldNameExpression("DeptId")));
        Predicate pred2 = new Predicate(new Term(new FieldNameExpression("CId"), new FieldNameExpression("CourseId")));

        // Apply selection predicates
        Scan s6 = new SelectScan(s5, pred1);
        Scan s7 = new SelectScan(s6, pred2);

        // Project the final result
        Collection<String> fieldlist = Arrays.asList("DName", "Title", "Prof");
        Scan sTop = new ProjectScan(s7, fieldlist);

        // Execute the final scan
        sTop.beforeFirst(); // Ensure the scan is positioned before the first record
        while (sTop.next()) {
            System.out.println(sTop.getString("DName") + "," +
                    sTop.getString("Title") + "," + sTop.getString("Prof"));
        }

        sTop.close();
        tx.commit();
    }
}
