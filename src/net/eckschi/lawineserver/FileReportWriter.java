package src.net.eckschi.lawineserver;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


public class FileReportWriter extends ReportWriter 
{	
	public FileReportWriter(String name)
	{
    	DateFormat df = new SimpleDateFormat("yyyyMMdd");
    	Date date = new Date();
        
    	mDir = Settings.Instance().GetDataDir() + name + "/" +  df.format(date)+"/";
    	boolean success = (new File(mDir)).mkdirs();
        if (success) 
        {
        	System.out.println("Directory: " + mDir + " created");
        }
        else 
        {
        	System.out.println("Directory: " + mDir + " NOT created");
        }
	}
	
	public void Write(AvalancheReport ar)
	{   	       
        Element e = null;
//        Node n = null;

        Document xmldoc = new DocumentImpl();
        //Root element.
        Element root = xmldoc.createElement("report");


        e = xmldoc.createElementNS(null, "author");
        e.setAttributeNS(null, "text", ar.GetAuthor());
        root.appendChild(e);

        e = xmldoc.createElementNS(null, "date");
        e.setAttributeNS(null, "text", ar.GetDate());
        root.appendChild(e);

        e = xmldoc.createElementNS(null, "title");
        e.setAttributeNS(null, "text", ar.GetTitle());
        root.appendChild(e);

        e = xmldoc.createElementNS(null, "copyright");
        e.setAttributeNS(null, "text", ar.GetCopyright());
        root.appendChild(e);

        e = xmldoc.createElementNS(null, "map");
        e.setAttributeNS(null, "text", ar.GetImgOnDevice().substring(ar.GetImgOnDevice().lastIndexOf('/')+1));
        root.appendChild(e);

        e = xmldoc.createElementNS(null, "content");
        e.setAttributeNS(null, "text", ar.GetContent());
        root.appendChild(e);

        
        
        //        n = xmldoc.createTextNode(desc[i]);
//        e.appendChild(n);

//        String[] id = {"PWD122","MX787","A4Q45"};
//        String[] type = {"customer","manager","employee"};
//        String[] desc = {"Tim@Home","Jack&Moud","John D'oé"};
//        for (int i=0;i<id.length;i++)
//        {
//        // Child i.
//        }

        xmldoc.appendChild(root);

        // write out
        try
        {
        	FileOutputStream fos = new FileOutputStream(mDir+"report.xml");
        	//XERCES 1 or 2 additionnal classes.
        	OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
        	of.setIndent(1);
        	of.setIndenting(true);
        	of.setDoctype(null,"users.dtd");
        	XMLSerializer serializer = new XMLSerializer(fos,of);
        	//As a DOM Serializer
        	serializer.asDOMSerializer();
        	serializer.serialize( xmldoc.getDocumentElement() );
        	fos.close();
        }
        catch (Exception ex)
        {		
        	System.out.println(ex.toString());
        }
	}
}
