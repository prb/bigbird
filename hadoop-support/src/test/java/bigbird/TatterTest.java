package bigbird;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;
import org.junit.Test;

public class TatterTest extends AbstractHBaseTest {
    @Test
    public void testFoo() throws Exception {
        HTable table = new HTable(conf, "tats");
        

        // To do any sort of update on a row, you use an instance of the BatchUpdate
        // class. A BatchUpdate takes a row and optionally a timestamp which your
        // updates will affect. 
        BatchUpdate batchUpdate = new BatchUpdate("myRow");

        // The BatchUpdate#put method takes a Text that describes what cell you want
        // to put a value into, and a byte array that is the value you want to 
        // store. Note that if you want to store strings, you have to getBytes() 
        // from the string for HBase to understand how to store it. (The same goes
        // for primitives like ints and longs and user-defined classes - you must 
        // find a way to reduce it to bytes.)
        batchUpdate.put("myColumnFamily:columnQualifier1", 
          "columnQualifier1 value!".getBytes());

        // Once you've done all the puts you want, you need to commit the results.
        // The HTable#commit method takes the BatchUpdate instance you've been 
        // building and pushes the batch of changes you made into HBase.
        table.commit(batchUpdate);

        // Now, to retrieve the data we just wrote. The values that come back are
        // Cell instances. A Cell is a combination of the value as a byte array and
        // the timestamp the value was stored with. If you happen to know that the 
        // value contained is a string and want an actual string, then you must 
        // convert it yourself.
        Cell cell = table.get("myRow", "myColumnFamily:columnQualifier1");
        String valueStr = new String(cell.getValue());
        System.out.println("GOT " + valueStr);
        
        // Sometimes, you won't know the row you're looking for. In this case, you
        // use a Scanner. This will give you cursor-like interface to the contents
        // of the table.
        Scanner scanner = 
          // we want to get back only "myColumnFamily:columnQualifier1" when we iterate
          table.getScanner(new String[]{"myColumnFamily:columnQualifier1"});
        
        
        // Scanners in HBase 0.2 return RowResult instances. A RowResult is like the
        // row key and the columns all wrapped up in a single interface. 
        // RowResult#getRow gives you the row key. RowResult also implements 
        // Map, so you can get to your column results easily. 
        
        // Now, for the actual iteration. One way is to use a while loop like so:
        RowResult rowResult = scanner.next();
        
        while(rowResult != null) {
          // print out the row we found and the columns we were looking for
          System.out.println("Found row: " + new String(rowResult.getRow()) + " with value: " +
           rowResult.get("myColumnFamily:columnQualifier1".getBytes()));
          
          rowResult = scanner.next();
        }
        
        // The other approach is to use a foreach loop. Scanners are iterable!
        for (RowResult result : scanner) {
          // print out the row we found and the columns we were looking for
          System.out.println("Found row: " + new String(result.getRow()) + " with value: " +
           result.get("myColumnFamily:columnQualifier1".getBytes()));
        }
        
        // Make sure you close your scanners when you are done!
        scanner.close();
    }
}
