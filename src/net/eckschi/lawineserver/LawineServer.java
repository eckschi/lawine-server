package src.net.eckschi.lawineserver;

// http://www.coderanch.com/t/469154/GUI/java/Runnable-jar-not-including-files


import com.memetix.mst.translate.Translate;

public class LawineServer 
{
	public final static String[] Locations =
	{
		"salzburg", 
		"styria", 
/*		"tyrol",
		"southtyrol",
*/	//	"carinthia",

		"vorarlberg",
		"upperaustria",
		"loweraustria", 
		/*
		"bavaria", 
		"veneto",
		"trentino", 
		"switzerland",
/*		"en/salzburg",
		"en/styria",
		"en/tyrol",
		"en/southtyrol",
		"en/carinthia",
		"en/vorarlberg",
		"en/upperaustria",
		"en/loweraustria",
		"en/bavaria", 
		"en/veneto",
		"en/trentino", 
		"en/switzerland",*/
	};
	
	public static boolean upload = true;

	public static final void main(String[] args)
    {
		// Set the Microsoft Translator API Key - Get yours at http://www.bing.com/developers/createapp.aspx
		Translate.setKey("4C2B4C498E936A3FEEB32F247DEC58CF5EBBAF50");

		// since there's no other argument -> quick and dirty 
		if (args.length > 0)
			Settings.Instance().NewValues();
		
    	String [] directories = new String[Locations.length];
    	String [] mapFiles = new String[Locations.length];
    	
    	for (int i=0; i<Locations.length; i++)
    	{
    		AvalancheReport ar = null;
        	ReportWriter rw = new FileReportWriter(Locations[i]);
        	if (Locations[i].equals("salzburg")) ar = new FetchSalzburg(rw); 
        	else if (Locations[i].equals("styria")) ar = new FetchStyria(rw); 
        	else if (Locations[i].equals("tyrol")) ar = new FetchTyrol(rw);
        	else if (Locations[i].equals("southtyrol")) ar = new FetchSouthTyrol(rw);
        	else if (Locations[i].equals("carinthia")) ar = new FetchCarinthia(rw);
        	else if (Locations[i].equals("vorarlberg")) ar = new FetchVorarlberg(rw);
        	else if (Locations[i].equals("upperaustria")) ar = new FetchUpperAustria(rw);
        	else if (Locations[i].equals("loweraustria")) ar = new FetchLowerAustria(rw);
        	else if (Locations[i].equals("bavaria")) ar = new FetchBavaria(rw);
        	else if (Locations[i].equals("veneto")) ar = new FetchVeneto(rw);
        	else if (Locations[i].equals("trentino")) ar = new FetchTrentino(rw);
        	else if (Locations[i].equals("switzerland")) ar = new FetchSwitzerland(rw);
        	else if (Locations[i].equals("en/salzburg")) ar = new FetchSalzburgEn(rw);
        	else if (Locations[i].equals("en/styria")) ar = new FetchStyriaEn(rw); 
/*        	else if (Locations[i].equals("en/tyrol")) ar = new FetchTyrolEn(rw);
        	else if (Locations[i].equals("en/southtyrol")) ar = new FetchSouthTyrolEn(rw);
        	else if (Locations[i].equals("en/carinthia")) ar = new FetchCarinthiaEn(rw);
        	else if (Locations[i].equals("en/vorarlberg")) ar = new FetchVorarlbergEn(rw);
        	else if (Locations[i].equals("en/upperaustria")) ar = new FetchUpperAustriaEn(rw);
        	else if (Locations[i].equals("en/loweraustria")) ar = new FetchLowerAustriaEn(rw);
        	else if (Locations[i].equals("en/bavaria")) ar = new FetchBavariaEn(rw);
        	else if (Locations[i].equals("en/veneto")) ar = new FetchVenetoEn(rw);
        	else if (Locations[i].equals("en/trentino")) ar = new FetchTrentinoEn(rw);
        	else if (Locations[i].equals("en/switzerland")) ar = new FetchSwitzerlandEn(rw);
 */       	
        	if (ar != null) 
        	{
        		HtmlFileWriter h = new HtmlFileWriter(Locations[i]);
        		h.Write(ar);
        		rw.Write(ar);
        	}

        	// cache for ftp upload
        	directories[i] = rw.GetDestinationDir();
        	mapFiles[i] = ar.GetImgOnDevice();
        	try
        	{
        		Utils.filecopy(mapFiles[i], Settings.Instance().GetDataDir()+Locations[i]+"/");
        	}
        	catch (Exception e)
        	{}
    	}

    	if (upload)
    	{
    		// ftp upload
    		FtpUploader ftp = new FtpUploader();
    		ftp.Connect();
    		//ftp.CreateDirectoryStructure(Locations);

    		for (int i=0; i<directories.length; i++)
    		{
    			ftp.Upload(directories[i]+"/report.xml", Locations[i]+"/report.xml");
    			ftp.Upload(mapFiles[i], Locations[i]+mapFiles[i].substring(mapFiles[i].lastIndexOf('/')));	
    			ftp.Upload(Settings.Instance().GetDataDir()+Locations[i]+".html", Locations[i]+".html");
    		}
    		ftp.Disconnect();
    	}
    }
}


