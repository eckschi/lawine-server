package src.net.eckschi.lawineserver;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;



public class FetchUpperAustria extends AvalancheReport 
{

	private enum eState 
	{
		eFindDate,
		eFindDate2,
		eDate,
		eFindExposition,
		eFindLevel1,
		eLevel1,
		eFindLevel2,
		eLevel2,		
		eFindTitle,
		eTitle,
		eContent,
		eNada3
	};
	
	private eState mState = eState.eFindDate; 
	private String mExposition; 
	private String mLevel1, mLevel2;
	private int mCnt = 0;
	
	private final String USER_AGENT = "Mozilla/5.0";
	// HTTP POST request
	private void sendPost() throws Exception {
 
		//String url = "https://selfsolve.apple.com/wcResults.do";
		String url = "http://www2.land-oberoesterreich.gv.at/lnw/LNWLawinenberichtBearbeiten.jsp";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
		String urlParameters = "caamlXml=Caaml+Export";
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
 


	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------	
	public FetchUpperAustria(ReportWriter rw)
	{
		super();
		mUrlStr = "http://www2.land-oberoesterreich.gv.at/lnw/LNWCAAMLAktuellServlet";
		mEncoding = "ISO-8859-1";
		mCopyright = "© Land Oberösterreich"; 
		mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
  
		try
		{
			System.out.println(mUrlStr);
			URL url = new URL(mUrlStr);
			long startTime = System.currentTimeMillis();
			HttpURLConnection c = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
			c.setRequestMethod("GET");
	        c.setDoOutput(true);
	        c.setReadTimeout(10000);
			c.connect();
			InputStream ist = c.getInputStream();
	        System.out.println("finished in "
	                + ((System.currentTimeMillis() - startTime) / 1000)
	                + " sec");
			
	        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = builderFactory.newDocumentBuilder();
	        Document document = builder.parse(ist);
	        XPath xPath =  XPathFactory.newInstance().newXPath();
	        
	        // content
	        String expression = "/CaamlData/observations/Bulletin/bulletinResultsOf/BulletinMeasurements/comment";
	        //read a string value
	        mContent = xPath.compile(expression).evaluate(document);
	        String weatherExp = "/CaamlData/observations/Bulletin/bulletinResultsOf/BulletinMeasurements/wxSynopsisHighlights";
	        mContent += "<br><b>Wetter:</b><br>" + xPath.compile(weatherExp).evaluate(document);        
	        String structureExp = "/CaamlData/observations/Bulletin/bulletinResultsOf/BulletinMeasurements/snowpackStructureComment";
	        mContent += "<br><b>Schneedeckenaufbau:</b><br>" + xPath.compile(structureExp).evaluate(document);
	        String tendencyExp = "/CaamlData/observations/Bulletin/bulletinResultsOf/BulletinMeasurements/travelAdvisoryComment";
	        mContent += "<br><b>Tendenz:</b><br>" + xPath.compile(tendencyExp).evaluate(document);
	        System.out.println(mContent);

	        String titleExp = "/CaamlData/observations/Bulletin/bulletinResultsOf/BulletinMeasurements/highlights";
	        //read a string value
	        mTitle = xPath.compile(titleExp).evaluate(document);
	        System.out.println(mTitle);
	
	        String dateExp = "/CaamlData/observations/Bulletin/metaDataProperty/MetaData/dateTimeReport";
	        String datestr = xPath.compile(dateExp).evaluate(document);
	        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
	        DateFormat outformat = new SimpleDateFormat("dd.MM.yyyy");
	        mDate = outformat.format(df.parse(datestr));
	        System.out.println(mDate);
       
        

        //InputSource is = new InputSource(ist);
		//is.setEncoding(mEncoding);
		}
		catch (Exception e)
		{

		}
	}
	
    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	private void prepareImage(String dest)
	{
		try
		{
			// merge	 	
			BufferedImage img = ImageIO.read(getClass().getResourceAsStream("/resources/upperaustria/landscape.jpg"));

			if (img == null) System.out.println("something wicked happend");
             
			Graphics2D vCanvas = img.createGraphics();

			// windrose
			Image windroseBmp = ImageIO.read(getClass().getResourceAsStream("/resources/upperaustria/"+mExposition));		
			vCanvas.drawImage(windroseBmp/*windroseTransparent*/, 290, 200, null);

			System.out.println("Levels: " + mLevel1 + " " + mLevel2);
			Image Level1Bmp = ImageIO.read(getClass().getResourceAsStream("/resources/upperaustria/"+mLevel1));
			vCanvas.drawImage(Level1Bmp, 105, 65, null);
			Image Level2Bmp = ImageIO.read(getClass().getResourceAsStream("/resources/upperaustria/"+mLevel2));
			vCanvas.drawImage(Level2Bmp, 250, 105, null);
			
			vCanvas.setPaint(new Color(0,0,0));
			vCanvas.setFont(new Font("Arial", Font.BOLD, 10));
			vCanvas.drawString("Besonders gef√§hrdete", 140, 225);
			vCanvas.drawString("Hangrichtung (schwarz)", 140, 245);
			
			// write back
			ImageIO.write(img, "jpeg", new File(dest));
		}
		catch (Exception e)
		{
			System.out.println("error " + e.getMessage());	
        }
	}
}
