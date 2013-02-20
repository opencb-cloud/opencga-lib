package org.bioinfo.opencga.lib;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.bioinfo.opencga.lib.account.CloudSessionManager;
import org.bioinfo.opencga.lib.account.io.IOManagementException;
import org.bioinfo.opencga.lib.storage.Indexer;
import org.bioinfo.opencga.lib.utils.Config;
import org.junit.Test;

public class IndexerManagerTest {
	private static Logger logger = Logger.getLogger(CloudSessionManager.class);
	@Test
	public void testSortBam() throws IOManagementException, IOException, InterruptedException {
		Config.configureLog4j();
		Indexer indexerManager = new Indexer();
		
//		indexerManager.createBamIndex(Paths.get("/httpd/bioinfo/gcsa/accounts/paco/buckets/default/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam"));
	}

}
