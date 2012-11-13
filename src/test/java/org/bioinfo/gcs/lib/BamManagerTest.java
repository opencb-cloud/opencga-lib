package org.bioinfo.gcs.lib;

import java.io.IOException;

import org.bioinfo.gcsa.lib.storage.alignment.BamManager;
import org.junit.Test;

public class BamManagerTest {

	@Test
	public void test() {
		try {
			BamManager bu = new BamManager();
			String result = bu.getByRegion("/home/examples","HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114", "20",168414,168414);
//			String result = bu.getByRegion("/home/examples","out_sorted", "1", 90604245, 93604245);
//			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
