package src.net.eckschi.lawineserver;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class HtmlFileWriter extends ReportWriter 
{
	String mFilename;
	String mName;
	
	public HtmlFileWriter(String name)
	{
		if (name.startsWith("en/"))
		{	
			mName = name.substring(3);
			mFilename = Settings.Instance().GetDataDir() + mName + "_en.html";
		}
		else
		{	
			mName = name;
			mFilename = Settings.Instance().GetDataDir() + mName +".html";
		}
	}
	
	public void Write(AvalancheReport ar)
	{
        String summary = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"> <html> <head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />"+ 
        "<meta http-equiv=\"Cache-Control\" content=\"no-cache\" />" +
        "<meta http-equiv=\"Expires\" content=\"0\" />" +
        "</head><body> <strong>" + ar.GetTitle() +" </strong> <br>" + 
        ar.GetDate() + "<br>" + 
        "<img src=\"" + mName + "/map" + ar.GetImgOnDevice().substring(ar.GetImgOnDevice().lastIndexOf(".")) +
	    "\" width=\"100%\" alt=\"nada\" />" + 
	    ar.GetContent() + 
	    "<br><br>" + ar.GetAuthor() + 
	    "<br><small>" + ar.GetCopyright() + "</small></body></html>";
		// write out
        try
        {       	
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(mFilename), "UTF-8");           
            out.write(summary);
            out.close();
        }
        catch (Exception ex)
        {		
        	System.out.println(ex.toString());
        }
	}
}
