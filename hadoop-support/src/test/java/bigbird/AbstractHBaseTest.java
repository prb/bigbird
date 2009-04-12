package bigbird;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.dfs.MiniDFSCluster;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestCase;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MiniHBaseCluster;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.FSUtils;
import org.junit.After;
import org.junit.Before;

public class AbstractHBaseTest {
    private static final Log LOG = LogFactory.getLog(HBaseTestCase.class);

    protected volatile HBaseConfiguration conf;
    protected MiniDFSCluster dfsCluster;
    protected MiniHBaseCluster cluster;

    private boolean localfs;

    private FileSystem fs;

    private Path testDir;

    @Before
    public void setUp() throws Exception {
        setupConfiguration();
        setupDFS();
        setupHBase();
    }

    private void setupDFS() throws IOException {

        // start up the dfs
        dfsCluster = new MiniDFSCluster(conf, 2, true, (String[])null);

        // mangle the conf so that the fs parameter points to the minidfs we
        // just started up
        FileSystem filesystem = dfsCluster.getFileSystem();
        conf.set("fs.default.name", filesystem.getUri().toString());
        Path parentdir = filesystem.getHomeDirectory();
        conf.set(HConstants.HBASE_DIR, parentdir.toString());
        filesystem.mkdirs(parentdir);
        FSUtils.setVersion(filesystem, parentdir);
    }

    private void setupConfiguration() throws Exception {
        conf = new HBaseConfiguration();
        localfs = (conf.get("fs.default.name", "file:///").compareTo("file:///") == 0);

        if (fs == null) {
            this.fs = FileSystem.get(conf);
        }
        
        try {
            if (localfs) {
                this.testDir = getUnitTestdir();
                if (fs.exists(testDir)) {
                    fs.delete(testDir, true);
                }
            } else {
                this.testDir = this.fs.makeQualified(new Path(conf.get(HConstants.HBASE_DIR)));
            }
        } catch (Exception e) {
            LOG.fatal("error during setup", e);
            throw e;
        }
    }

    protected Path getUnitTestdir() {
      return new Path("target/data");
    }

    protected void setupHBase() throws Exception {
        // start the mini cluster
        this.cluster = new MiniHBaseCluster(conf, 1);
        // opening the META table ensures that cluster is running
        new HTable(conf, HConstants.META_TABLE_NAME);
    }

    @After
    protected void tearDown() throws Exception {
        try {
            if (localfs) {
                if (this.fs.exists(testDir)) {
                    this.fs.delete(testDir, true);
                }
            }

            HConnectionManager.deleteConnectionInfo(conf, true);
            if (this.cluster != null) {
                try {
                    this.cluster.shutdown();
                } catch (Exception e) {
                    LOG.warn("Closing mini dfs", e);
                }
            }
            shutdownDfs(dfsCluster);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Common method to close down a MiniDFSCluster and the associated file system
     * 
     * @param cluster
     */
    public static void shutdownDfs(MiniDFSCluster cluster) {
        if (cluster != null) {
            try {
                FileSystem fs = cluster.getFileSystem();
                if (fs != null) {
                    LOG.info("Shutting down FileSystem");
                    fs.close();
                }
            } catch (IOException e) {
                LOG.error("error closing file system", e);
            }

            LOG.info("Shutting down Mini DFS ");
            try {
                cluster.shutdown();
            } catch (Exception e) {
                // / Can get a java.lang.reflect.UndeclaredThrowableException thrown
                // here because of an InterruptedException. Don't let exceptions in
                // here be cause of test failure.
            }
        }
    }
}
