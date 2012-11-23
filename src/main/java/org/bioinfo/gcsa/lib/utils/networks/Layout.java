package org.bioinfo.gcsa.lib.utils.networks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.bioinfo.commons.Config;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;

public class Layout {
	protected Config config;
	protected Logger logger;
	protected String homePath;
	
	static HashSet<String> graphvizLayoutAlgorithms;
	static HashMap<String, String> graphvizOutputFormats;

	static {
		graphvizLayoutAlgorithms = new HashSet<String>();
		graphvizLayoutAlgorithms.add("circo");
		graphvizLayoutAlgorithms.add("dot");
		graphvizLayoutAlgorithms.add("fdp");
		graphvizLayoutAlgorithms.add("neato");
		graphvizLayoutAlgorithms.add("osage");
		graphvizLayoutAlgorithms.add("sfdp");
		graphvizLayoutAlgorithms.add("twopi");
	}

	static {
		graphvizOutputFormats = new HashMap<String, String>();
		graphvizOutputFormats.put("dot", "text");
		graphvizOutputFormats.put("jpg", "jpeg");graphvizOutputFormats.put("jpeg", "jpeg");graphvizOutputFormats.put("jpe", "jpeg");
		graphvizOutputFormats.put("svg", "svg");graphvizOutputFormats.put("svgz", "zip");
		graphvizOutputFormats.put("png", "png");
		graphvizOutputFormats.put("plain", "plain");
	}
	
	public Layout() throws IOException {
		homePath = System.getenv("GCSA_HOME");
		config = new Config(homePath + "/conf/utils.properties");
		
		logger = new Logger();
		logger.setLevel(Integer.parseInt(config.getProperty("UTILS.LOG.LEVEL")));
	}
	
	public LayoutResp layout(String layoutAlgorithm, String outputFormat, String dotData, String filename, String base64, String jsonpCallback) {
		LayoutResp resp = new LayoutResp();
		
		logger.debug("LayoutWSServer: layout() method");
		if(graphvizLayoutAlgorithms.contains(layoutAlgorithm)) {
			if(graphvizOutputFormats.containsKey(outputFormat)) {
				if(dotData != null && !dotData.equals("")) {
					logger.debug("Algorithm layout: "+layoutAlgorithm+", output format: "+outputFormat+", dot: "+dotData);
					try {
						logger.info("defaultConfig:"+config.toString());
						String randomFolder = config.getProperty("TMP.FOLDER")+"/"+StringUtils.randomString(20)+"_layout";
						logger.debug("Creating output folder: "+randomFolder);
						FileUtils.createDirectory(randomFolder);

						String inputFile = randomFolder+"/input.dot";
						String outputFile = randomFolder+"/"+filename+"."+outputFormat;
						logger.debug("Writting dot data file: "+inputFile);
						IOUtils.write(inputFile, dotData);

						int exitValue = executeGraphviz(new File(inputFile), layoutAlgorithm, outputFormat, new File(outputFile));
						if(exitValue == 0) {
							FileUtils.checkFile(outputFile);
							if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
								logger.debug("Encoding in Base64 the dot output file...");
								byte[] binaryBytes = toByteArray(new FileInputStream(outputFile));
								byte[] base64Bytes = Base64.encodeBase64(binaryBytes);
								String encodedString = new String(base64Bytes);

								if(jsonpCallback != null && !jsonpCallback.equals("")) {
//									return Response.ok("var " + jsonpCallback + " = (" + encodedString + ")", MediaType.APPLICATION_JSON_TYPE).build();
									resp.setData("var " + jsonpCallback + " = (" + encodedString + ")");
									resp.setType("json");
									return resp;
								}else {
									//									return Response.ok(encodedString, MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+"."+outputFormat).build();
//									return Response.ok(encodedString, MediaType.TEXT_PLAIN).build();
									resp.setData(encodedString);
									resp.setType("text");
									return resp;
								}
							}else {	
								// returning the Graphviz output file
								byte[] bytes = toByteArray(new FileInputStream(new File(outputFile)));
//								return Response.ok(bytes, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition","attachment; filename = "+filename+"."+outputFormat).build();
//								return createOkResponse(bytes, MediaType.APPLICATION_OCTET_STREAM_TYPE, filename+"."+outputFormat);
								resp.setData(bytes);
								resp.setType("bytes");
								resp.setFileName(filename+"."+outputFormat);
								return resp;
							}
						}else {
//							return Response.ok("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
//							return createOkResponse("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
							resp.setData("Graphviz exit status not 0: '"+exitValue+"'");
							resp.setType("text");
							resp.setFileName(filename+".err.log");
							return resp;
						}
					} catch (Exception e) {
						logger.error("Error in LayoutWSServer, layout() method: " + StringUtils.getStackTrace(e));
						if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//							return Response.ok("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN).build();
//							return createOkResponse("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN_TYPE);
							resp.setData("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e));
							resp.setType("text");
							return resp;
						}else {
//							return Response.ok("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
//							return createOkResponse("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
							resp.setData("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e));
							resp.setType("text");
							resp.setFileName(filename+".err.log");
							return resp;
						}
					}
				}else {
					if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//						return Response.ok("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN).build();
//						return createOkResponse("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
						resp.setData("dot data '"+dotData+"' is not valid");
						resp.setType("text");
						return resp;
					}else {
//						return Response.ok("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
//						return createOkResponse("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
						resp.setData("dot data '"+dotData+"' is not valid");
						resp.setType("text");
						resp.setFileName(filename+".err.log");
						return resp;
					}
				}
			}else {
				if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//					return Response.ok("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN).build();
//					return createOkResponse("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
					resp.setData("Format '"+outputFormat+"' is not valid");
					resp.setType("text");
					return resp;
				}else {
//					return Response.ok("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
//					return createOkResponse("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
					resp.setData("Format '"+outputFormat+"' is not valid");
					resp.setType("text");
					resp.setFileName(filename+".err.log");
					return resp;
				}
			}
		}else {
			if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//				return Response.ok("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN).build();
//				return createOkResponse("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
				resp.setData("Algorithm '"+layoutAlgorithm+"' is not valid");
				resp.setType("text");
				return resp;
			}else {
//				return Response.ok("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
//				return createOkResponse("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
				resp.setData("Algorithm '"+layoutAlgorithm+"' is not valid");
				resp.setType("text");
				resp.setFileName(filename+".err.log");
				return resp;
			}
		}
	}
	
	public LayoutResp coordinates(String layoutAlgorithm, String dotData, String jsonpCallback) {
		LayoutResp resp = new LayoutResp();
		
		logger.debug("LayoutWSServer:  coordinates() method");
		if(graphvizLayoutAlgorithms.contains(layoutAlgorithm)) {
			StringBuilder sb = new StringBuilder("{");
			try {
				String randomFolder = config.getProperty("TMP.FOLDER")+"/"+StringUtils.randomString(20)+"_layout";
				logger.debug("Creating output folder: "+randomFolder);
				FileUtils.createDirectory(randomFolder);

				String inputFile = randomFolder+"/input.dot";
				String outputFile = randomFolder+"/output.plain";
				logger.debug("Writting dot data file: "+inputFile);
				IOUtils.write(inputFile, dotData);

				int exitValue = executeGraphviz(new File(inputFile), layoutAlgorithm, "plain", new File(outputFile));
				if(exitValue == 0) {
					FileUtils.checkFile(outputFile);
					// getting the coords form the file
					List<String> lines = IOUtils.grep(new File(outputFile), "^node.+");
					String[] fields;
					double min = Double.POSITIVE_INFINITY;
					double max = Double.NEGATIVE_INFINITY;
					String[] ids = new String[lines.size()];
					double[][] coords = new double[lines.size()][2];
					for(int i=0; i<lines.size(); i++) {
						fields = lines.get(i).split(" ");
						ids[i] = fields[1];
						coords[i][0] = Double.parseDouble(fields[2]);
						coords[i][1] = Double.parseDouble(fields[3]);
						min = Math.min(min, Math.min(coords[i][0], coords[i][1]));
						max = Math.max(max, Math.max(coords[i][0], coords[i][1]));
					}
					// max needs to be calculated after subtract min
					max -= min;
					for(int i=0; i<ids.length; i++) {
						sb.append("\""+ids[i]+"\"").append(": {").append("\"id\":\"").append(ids[i]).append("\", \"x\": ").append((coords[i][0]-min)/max).append(", \"y\": ").append((coords[i][1]-min)/max).append("}");
						if(i < ids.length-1) {
							sb.append(", ");
						}
					}
					sb.append("}");

					if(jsonpCallback != null && !jsonpCallback.equals("")) {
//						return Response.ok("var " + jsonpCallback + " = (" + sb.toString() + ")", MediaType.APPLICATION_JSON_TYPE).build();
//						return createOkResponse("var " + jsonpCallback + " = (" + sb.toString() + ")", MediaType.APPLICATION_JSON_TYPE);
						resp.setData("var " + jsonpCallback + " = (" + sb.toString() + ")");
						resp.setType("json");
						return resp;
					}else {
//						return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
//						return createOkResponse(sb.toString(), MediaType.TEXT_PLAIN_TYPE);
						resp.setData(sb.toString());
						resp.setType("text");
						return resp;
					}
				}else {
//					return Response.ok("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN).build();
//					return createOkResponse("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN_TYPE);
					resp.setData("Graphviz exit status not 0: '"+exitValue+"'");
					resp.setType("text");
					return resp;
				}
			} catch (Exception e) {
				logger.error("Error in LayoutWSServer, layout() method: " + StringUtils.getStackTrace(e));
//				return Response.ok("Error in LayoutWSServer, coordinates() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN).build();
//				return createOkResponse("Error in LayoutWSServer, coordinates() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN_TYPE);
				resp.setData("Error in LayoutWSServer, coordinates() method:\n"+StringUtils.getStackTrace(e));
				resp.setType("text");
				return resp;
			}
		}else {
//			return Response.ok("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN).build();
//			return createOkResponse("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
			resp.setData("Algorithm '"+layoutAlgorithm+"' is not valid");
			resp.setType("text");
			return resp;
		}
	}
	
	private int executeGraphviz(File inputFile, String layoutAlgorithm, String outputFormat, File outputFile) throws IOException, InterruptedException {
		FileUtils.checkFile(inputFile);
		FileUtils.checkDirectory(outputFile.getParent());
		String command = "dot -K"+layoutAlgorithm+" -T"+outputFormat +" -o"+outputFile+" "+inputFile;
		logger.debug("Graphviz command line: "+command);
		Process process= Runtime.getRuntime().exec(command);
		process.waitFor();
		logger.debug("Graphviz exit status: "+process.exitValue());
		return process.exitValue();
	}
	
	protected byte[] toByteArray(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024*4];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int n = 0;
		while ((n = inputStream.read(buffer)) != -1) {
			output.write(buffer, 0, n);
		}
		output.flush();
		return output.toByteArray();
	}
	
	public class LayoutResp {
		private Object data;
		private String type, fileName;
		
		public LayoutResp(){
			
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
}
