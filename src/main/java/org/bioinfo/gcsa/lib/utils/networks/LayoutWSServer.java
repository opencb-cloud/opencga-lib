package org.bioinfo.gcsa.lib.utils.networks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.utils.ws.server.rest.GenericRestToolWSServer;

import com.sun.jersey.core.util.Base64;

@Produces("text/plain")
@Path("/network/layout")
public class LayoutWSServer extends GenericRestToolWSServer {

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

	public LayoutWSServer(@Context UriInfo uriInfo) {
		super(uriInfo);
		defaultConfig = ResourceBundle.getBundle("org.bioinfo.utils.ws.application");	
		logger.setLevel(1);
	}

	
	@POST
	@Path("/{algorithm}.{format}")
	public Response layout(@PathParam("algorithm") String layoutAlgorithm, @PathParam("format") String outputFormat, @FormParam("dot") String dotData, @DefaultValue("output") @FormParam("filename") String filename, @DefaultValue("false") @FormParam("base64") String base64, @FormParam("jsonp") String jsonpCallback) {
		logger.debug("LayoutWSServer: layout() method");
		if(graphvizLayoutAlgorithms.contains(layoutAlgorithm)) {
			if(graphvizOutputFormats.containsKey(outputFormat)) {
				if(dotData != null && !dotData.equals("")) {
					logger.debug("Algorithm layout: "+layoutAlgorithm+", output format: "+outputFormat+", dot: "+dotData);
					try {
						logger.info("defaultConfig:"+defaultConfig.toString());
						String randomFolder = defaultConfig.getString("TMP.FOLDER")+"/"+StringUtils.randomString(20)+"_layout";
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
								byte[] base64Bytes = Base64.encode(binaryBytes);
								String encodedString = new String(base64Bytes);

								if(jsonpCallback != null && !jsonpCallback.equals("")) {
//									return Response.ok("var " + jsonpCallback + " = (" + encodedString + ")", MediaType.APPLICATION_JSON_TYPE).build();
									return createOkResponse("var " + jsonpCallback + " = (" + encodedString + ")", MediaType.APPLICATION_JSON_TYPE);
								}else {
									//									return Response.ok(encodedString, MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+"."+outputFormat).build();
//									return Response.ok(encodedString, MediaType.TEXT_PLAIN).build();
									return createOkResponse(encodedString, MediaType.TEXT_PLAIN_TYPE);
								}
							}else {	
								// returning the Graphviz output file
								byte[] bytes = toByteArray(new FileInputStream(new File(outputFile)));
//								return Response.ok(bytes, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition","attachment; filename = "+filename+"."+outputFormat).build();
								return createOkResponse(bytes, MediaType.APPLICATION_OCTET_STREAM_TYPE, filename+"."+outputFormat);
							}
						}else {
//							return Response.ok("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
							return createOkResponse("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
						}
					} catch (Exception e) {
						logger.error("Error in LayoutWSServer, layout() method: " + StringUtils.getStackTrace(e));
						if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//							return Response.ok("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN).build();
							return createOkResponse("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN_TYPE);
						}else {
//							return Response.ok("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
							return createOkResponse("Error in LayoutWSServer, layout() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
						}
					}
				}else {
					if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//						return Response.ok("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN).build();
						return createOkResponse("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
					}else {
//						return Response.ok("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
						return createOkResponse("dot data '"+dotData+"' is not valid", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
					}
				}
			}else {
				if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//					return Response.ok("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN).build();
					return createOkResponse("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
				}else {
//					return Response.ok("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
					return createOkResponse("Format '"+outputFormat+"' is not valid", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
				}
			}
		}else {
			if(base64 != null && base64.trim().equalsIgnoreCase("true")) {
//				return Response.ok("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN).build();
				return createOkResponse("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
			}else {
//				return Response.ok("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN).header("content-disposition","attachment; filename = "+filename+".err.log").build();
				return createOkResponse("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN_TYPE, filename+".err.log");
			}
		}
	}

	@POST
	@Path("/{algorithm}.coords")
	public Response coordinates(@PathParam("algorithm") String layoutAlgorithm, @FormParam("dot") String dotData, @FormParam("jsonp") String jsonpCallback) {
		logger.debug("LayoutWSServer:  coordinates() method");
		if(graphvizLayoutAlgorithms.contains(layoutAlgorithm)) {
			StringBuilder sb = new StringBuilder("{");
			try {
				String randomFolder = defaultConfig.getString("TMP.FOLDER")+"/"+StringUtils.randomString(20)+"_layout";
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
						return createOkResponse("var " + jsonpCallback + " = (" + sb.toString() + ")", MediaType.APPLICATION_JSON_TYPE);
					}else {
//						return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
						return createOkResponse(sb.toString(), MediaType.TEXT_PLAIN_TYPE);
					}
				}else {
//					return Response.ok("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN).build();
					return createOkResponse("Graphviz exit status not 0: '"+exitValue+"'", MediaType.TEXT_PLAIN_TYPE);
				}
			} catch (Exception e) {
				logger.error("Error in LayoutWSServer, layout() method: " + StringUtils.getStackTrace(e));
//				return Response.ok("Error in LayoutWSServer, coordinates() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN).build();
				return createOkResponse("Error in LayoutWSServer, coordinates() method:\n"+StringUtils.getStackTrace(e), MediaType.TEXT_PLAIN_TYPE);
			}
		}else {
//			return Response.ok("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN).build();
			return createOkResponse("Algorithm '"+layoutAlgorithm+"' is not valid", MediaType.TEXT_PLAIN_TYPE);
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

}
