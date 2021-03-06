 
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;


public class queryAnalysis extends DefaultHandler {
   
  Hashtable  twigTagNames;
 	
 	static String filename;
 	
 	String ROOT;
 	
 	Stack TagStack;
 	
 	static String basicDocuemnt;

    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {
       
       twigTagNames = new Hashtable();
     
       TagStack = new Stack();
       
    }//end startDocument


	public void characters(char[] ch, int start,int length ) 
       {
       	String value = new String (ch,start,length);
       	
       	if (value.equalsIgnoreCase("1")) { //is PC relationship
       		String child = (String) TagStack.peek();
        	String parent = (String) TagStack.elementAt(TagStack.size()-2);
        	Vector temp = (Vector) twigTagNames.get(parent);
        	for(int i=0;i<temp.size();i++)
        		if    ((((QueryDataType)temp.elementAt(i)).getTagName().equalsIgnoreCase(child)))
        			{ ((QueryDataType)temp.elementAt(i)).setPCEdge(); break; }
        	
       	}//end if
       	
      	
        }// end characters

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
	throws SAXException
    { 	
        String currentTag = localName;
        
        if (TagStack.size()>0){
        	String parent = (String) TagStack.peek();
        	if (twigTagNames.containsKey(parent))
        	{	
        		Vector temp = (Vector) twigTagNames.get(parent);
        		QueryDataType data = new QueryDataType(currentTag,false);
        		temp.add(data);
        	}//end if
        	else
        	{	QueryDataType data = new QueryDataType(currentTag,false);
        		Vector temp = new Vector();
        		temp.add(data);
        		twigTagNames.put(parent,temp);
        	}//end else
        	
        }//end if
        else
        ROOT = currentTag;
        
        TagStack.push(currentTag);
       
      
    }//end startElement
    
    
     public void endElement(String namespaceURI, String localName,
                             String qName)
	throws SAXException
    {
    	
    	TagStack.pop();
        
    	
  	}//end endElement

    // Parser calls this once after parsing a document
    public void endDocument() throws SAXException {
    	
     //以下开始处理query，主要分为三步：一，进行query的分析
    	System.out.println( "begin analysis query !");
       
    	Query.setTwigTagNames(twigTagNames );
    	
    
    	Query.setRoot(ROOT);
    	
       utilities.DebugPrintln( "Query root is "+Query.getRoot());
      
       
    	 /*Hashtable h =Query.calculateBranchPosition();
    	
    	
   	Enumeration e = h.keys();
    	while (e.hasMoreElements())
    	{	String s = (String)e.nextElement();
    		int [] pos = (int [] )h.get(s);
    		utilities.DebugPrintln(s+ "position is "+pos[0]+" ; "+pos[1]);
    	}//end while
    	*/
    	System.out.println( "begin analysis document !");
    	
       try{ // 这里使用try，主要是为了调试方便。
      
    	 //二，把所有的数据从UTF8转换成整数形式放进内存，
    	DTDTable DTDInfor = loadDataSet.produceDTDInformation(basicDocuemnt);
    
      long totalbeginTime = System.currentTimeMillis();
    
		  Query.preComputing(DTDInfor); //这一步主要进行计算一些Query的特性以便计算使用
      
    	loadDataSet d = new loadDataSet();
    	System.out.println( "begin load data !");
    	
     	long loadbeginTime = System.currentTimeMillis();
    
      Hashtable [] alldata = d.loadAllLeafData (Query.getLeaves(),DTDInfor);
      
      long loadendTime = System.currentTimeMillis();
    
    	 //三，进行join的过程
   		System.out.println( "begin join !");
       
      long joinbeginTime = System.currentTimeMillis();
    
    	TwigSet join = new TwigSet(DTDInfor,alldata[1],alldata[0] );
    	
    	join.beginJoin();
    	
    	long joinendTime = System.currentTimeMillis();
    
    	long totalendTime = System.currentTimeMillis();
      	
       System.out.println("load data time is "+(loadendTime-loadbeginTime));
       	
       System.out.println("join data time is "+(joinendTime-joinbeginTime));
       	
       System.out.println("Total running time is "+(totalendTime-totalbeginTime));
       
    }catch(Exception e ){
    	e.printStackTrace();
    }//end catch
    	/*join.locateMatchedLabel("c");
    	join.advanceStream("d");
    	join.locateMatchedLabel("d");
    	join.advanceStream("b");
    	join.locateMatchedLabel("b");*/
    	
    	//join.MatchedPrefixes ("c","a" );// parameter format(leaf,branch)
		       
      
        }//end document
    
   
   

    /**
     * Convert from a filename to a file URL.
     */
    private static String convertToFileURL(String filename) {
        // On JDK 1.2 and later, simplify this to:
        // "path = file.toURL().toString()".
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    private static void usage() {
        System.err.println("Usage: QueryAnalysis <file.xml>");
        System.exit(1);
    }

    static public void main(String[] args) throws Exception {
       
         filename = args[0];
         
         basicDocuemnt = args[1];
       
        if (filename == null) {
            usage();
        }
        
        if (basicDocuemnt == null) {
            usage();
        }
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        
         spf.setNamespaceAware(true);
        
        // Create a JAXP SAXParser
        SAXParser saxParser = spf.newSAXParser();

        // Get the encapsulated SAX XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();

        // Set the ContentHandler of the XMLReader
        xmlReader.setContentHandler(new queryAnalysis());

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL(filename));
    }
    
    

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
