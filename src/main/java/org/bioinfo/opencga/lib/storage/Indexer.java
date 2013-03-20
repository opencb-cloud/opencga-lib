package org.bioinfo.opencga.lib.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.opencga.lib.analysis.AnalysisExecutionException;
import org.bioinfo.opencga.lib.analysis.SgeManager;
import org.bioinfo.opencga.lib.utils.Config;

public class Indexer {
	private static Logger logger = Logger.getLogger(Indexer.class);
	private static Path indexerManagerScript = Paths.get(Config.getGcsaHome(),
			Config.getAnalysisProperties().getProperty("OPENCGA.ANALYSIS.BINARIES.PATH"), "indexer", "indexerManager.py");

	public static String createBamIndex(Path inputBamPath) throws IOException, InterruptedException,
			AnalysisExecutionException {
		String jobId = StringUtils.randomString(8);
		String commandLine = indexerManagerScript + " -t bam " + inputBamPath;
		try {
			SgeManager.queueJob("indexer", jobId, 0, inputBamPath.getParent().toString(), commandLine);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new AnalysisExecutionException("ERROR: sge execution failed.");
		}
		return "indexer_"+jobId;
	}
}
