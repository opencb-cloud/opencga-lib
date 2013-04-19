package org.bioinfo.opencga.lib;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;
import org.bioinfo.opencga.lib.account.io.IOManagementException;
import org.bioinfo.opencga.lib.storage.datamanagers.bam.BamManager;
import org.bioinfo.opencga.lib.utils.Config;
import org.junit.Test;

public class BamManagerTest {

//	@Test
	public void test() throws IOManagementException {
		if(new File("/home/examples").exists()){
			try {
				BamManager bu = new BamManager();
				Path file = Paths.get("home","examples","bam","HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");
				Map<String, List<String>> params = new HashMap<String, List<String>> ();
//				view_as_pairs
//				show_softclipping
				params.put("show_softclipping", Arrays.asList("true"));
				String result = bu.getByRegion(file,"20:32875000-32879999", params );
//			String result = bu.getByRegion("/home/examples","out_sorted", "1", 90604245, 93604245);
//			System.out.println(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			System.out.println("/home/examples not found, skip.");
		}
	}
}
