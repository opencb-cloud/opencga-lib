#!/usr/bin/python -u

import sys, os, argparse, commands


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
        #print(tabixCmd)
        #print(tabixbgzipCmd)
    except:
        print("Environment variable GCSA_HOME is not set")
        sys.exit(-1)


def callOs(cmd):
    status, output = commands.getstatusoutput(cmd)
    print(str(status)+" -> "+output+" - "+cmd)
    if 35584 == status and "[bam_header_read]" in output:
        sys.exit(-1)
    #if "open: No such file or directory" in output:
        #sys.exit(-1)
    #if 256 == status and "mv" in output:
        #sys.exit(-1)
    #if 512 == status and "tar" in output:
        #sys.exit(-1)
    #if 512 == status and "sort" in output:
        #sys.exit(-1)
    #if 1 == status and "[bgzip]" in output:
        #sys.exit(-1)
    #if 1 == status and "[tabix]" in output:
        #sys.exit(-1)



#INDEXERS
def indexBAM(inputBAM, outdir):
    print("indexing bam file...")
    sortBam(inputBAM, outdir)
    callOs(samtoolsCmd + " index " + inputBAM)
    print("index complete!")

def sortBam(inputBAM, outdir):
    print("sorting bam file...")
    sortedBam = inputBAM + ".sort.bam"
    sortCmd = samtoolsCmd + " sort " + inputBAM + " " + inputBAM + ".sort"
    callOs(sortCmd)
    callOs("mv "+ sortedBam + " " + inputBAM)
    print("sort complete!")

def indexVCF(inputVCF, outdir):
    #file inputFVCF "gzip compressed"  zcat file |
    print("indexing vcf file...")
    callOs("tar -zcvf "+ inputVCF + ".tar.gz" + " " + inputVCF)
    callOs("sort -k1,1 -k2,2n "+ inputVCF + " > " + inputVCF + ".sort.vcf");
    callOs(tabixbgzipCmd + " -c " + inputVCF + ".sort.vcf" + " > " + inputVCF + ".gz")
    callOs(tabixCmd + " -p vcf " + inputVCF + ".gz")
    callOs("rm -rf "+ inputVCF + ".sort.vcf")
    print("index complete!")
    print("todo")




parser = argparse.ArgumentParser(prog="indexer")
parser.add_argument("type", choices=("bam","vcf"))
parser.add_argument("input", help="input file")
parser.add_argument("-c","--compressed", action="count", help="input file is gzipped")
parser.add_argument("-o", dest="outdir", help="destination folder")
args = parser.parse_args()


#outdir is the parent file dir
if args.outdir == None:
    args.outdir = os.path.abspath(os.path.join(args.input, os.path.pardir))

if os.path.isfile(args.input) is False:
    sys.exit(-1)

if os.path.isdir(args.outdir) is False:
    sys.exit(-1)

checkGcsaHome()


#print args.type
#print args.input
#print args.outdir

if args.type == "bam":
    indexBAM(args.input, args.outdir)

elif args.type == "vcf":
    indexVCF(args.input, args.outdir)



