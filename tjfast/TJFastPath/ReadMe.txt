This JAVA software matches a twig query using TJFast algorithm.

1. How to write a query ?

Write your desired query in query.xml file. We use "1" to present parent-child relationship and "0" to present ancestor-desendant relationship in the twig query.

For example:

(1). XPath expression:  a[/c]/b

query.xml :  <a><c>1</c><b>1</b></a>  

Note : no space allowed

(2). XPath expression:  a[/c][//d]/b

query.xml :  <a><c>1</c><d>0</d><b>1</b></a>  


(3). XPath expression:  a[//c]/b[//e]/d

query.xml :  <a><c>0</c><b>1<e>0</e><d>1</d></b></a>


2. Where to store join data ?

All join data (in the form of extended Dewey) are stored the folder "outputData". After you use "DataLoader" tool to generate the join data, please copy it into the folder "outputData".


3. How to run the program?

java -Xmx512M queryAnalysis  queryTwigPattern.xml source.xml (the source xml file name, which should be in the current folder)

For example:
java queryAnalysis queryTwigPattern.xml simpleTree.xml


4. At present, the total time of running includes the time for scanning the source document. But this time can be eliminated by storing the FST in the main memory. The real running time (excluding the time for scaning the source document) of TJFast is shown in the windows prompt.  
