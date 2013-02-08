package org.bioinfo.gcs.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bioinfo.gcsa.Config;
import org.bioinfo.gcsa.lib.account.CloudSessionManager;
import org.bioinfo.gcsa.lib.account.io.IOManagementException;
import org.bioinfo.gcsa.lib.storage.IndexerManager;
import org.bioinfo.gcsa.lib.storage.feature.BamManager;
import org.junit.Test;

public class IndexerManagerTest {
	private static Logger logger = Logger.getLogger(CloudSessionManager.class);
	@Test
	public void testSortBam() throws IOManagementException, IOException, InterruptedException {
		Config.configureLog4j();
		IndexerManager indexerManager = new IndexerManager();
		
//		indexerManager.createBamIndex(Paths.get("/httpd/bioinfo/gcsa/accounts/paco/buckets/default/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam"));
	}

}
