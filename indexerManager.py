#!/usr/bin/python -u

import sys, os, argparse


gcsaHome = "";
samtoolsCmd = "";
tabixCmd = "";
tabixbgzipCmd = "";

#Functions
def checkGcsaHome():
	try:
		global gcsaHome
		global samtoolsCmd
		global tabixCmd
		global tabixbgzipCmd
		#gcsaHome = os.environ["GCSA_HOME"]
		gcsaHome = "/httpd/bioinfo/gcsa"
		samtoolsCmd = gcsaHome +"/analysis/samtools/samtools"
		tabixCmd = gcsaHome +"/analysis/tabix/tabix"
		tabixbgzipCmd = gcsaHome +"/analysis/tabix/bgzip"
		print tabixCmd
		print tabixbgzipCmd
	except:
		print("Environment variable GCSA_HOME is not set")
		sys.exit(0)

def checkArgs():
	fileType = ""
	action = ""
	inputFile = ""
	outdir = ""
	if(len(sys.argv) > 1):
		fileType = sys.argv[1]
	else:
		printUsage()
	if(len(sys.argv) > 2):
		action = sys.argv[2]
	if(len(sys.argv) > 3):
		inputFile = sys.argv[3]
	if(len(sys.argv) > 4):
		outdir = sys.argv[4]
	return (fileType, action, inputFile, outdir)

def printUsage():
	print("usage: <type> <action> <input file> [outdir]")
	sys.exit(0)







#INDEXERS
def indexBAM(inputBAM, outdir):
	print("indexing bam file...")
	sortBam(inputBAM, outdir)
	indexCmd = samtoolsCmd + " index " + inputBAM
	os.system(indexCmd)
	print("index complete!")

def sortBam(inputBAM, outdir):
	print("sorting bam file...")
	sortedBam = inputBAM + ".sort.bam"
	sortCmd = samtoolsCmd + " sort " + inputBAM + " " + inputBAM + ".sort"
	os.system(sortCmd)
	os.system("mv "+ sortedBam + " " + inputBAM)
	print("sort complete!")

def indexVCF(inputVCF, outdir):
	print("indexing vcf file...")
	os.system("tar -zcvf "+ inputVCF + ".tar.gz" + " " + inputVCF)
	os.system("sort -k1,1 -k2,2n "+ inputVCF + " > " + inputVCF + ".sort.vcf");
	os.system(tabixbgzipCmd + " -c " + inputVCF + ".sort.vcf" + " > " + inputVCF + ".gz")
	os.system(tabixCmd + " -p vcf " + inputVCF + ".gz")
	os.system("rm -rf "+ inputVCF + ".sort.vcf")
	print("index complete!")
	print("todo")










#MAIN
#checkGcsaHome()
#(fileType, action, inputFile, outdir) = checkArgs()
#
#if fileType == "" or action == "" or inputFile == "" :
	#printUsage()
#
#
#if fileType == "bam":
	#if action == "sort":
		#sortBam(inputFile, outdir)
	#elif action == "index":
		#indexBAM(inputFile, outdir)
	#else:
		#printUsage()
#elif fileType == "vcf":
	#if action == "sort":
		#print("todo")
	#elif action == "index":
		#indexVCF(inputFile, outdir)
	#else:
		#printUsage()
#else:
	#print("option not allowed")


parser = argparse.ArgumentParser()
parser.parse_args()
