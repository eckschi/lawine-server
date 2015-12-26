package src.net.eckschi.lawineserver;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class FetchSouthTyrol extends AvalancheReport 
{
	private enum eState 
	{
		eFindDate,
		eDate,
		eFindTitle,
		eTitle,
		eFindImageLocation,
		eFindContent,
		eContent,
		eRiskLevel,
		eContent2,
		eSnowLevels,
		eFindTendency,
		eTendency,
		eFindAuthor,
		eAuthor,
		eNada3
	};
	
	private eState mState = eState.eFindDate; 
	private int mReportAge = 0;
	private String mImageURL1;
	private String mImageURL2;
	private String mTempImage1;
	private String mTempImage2;

	//-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchSouthTyrol(ReportWriter rw)
	{
		super();
		mUrlStr = "http://www.provinz.bz.it/lawinen/home.asp?detail=true";
		mEncoding = "UTF-8";
		mCopyright = "© 2012 Autonome Provinz Bozen - Südtirol | Lawinen";
		mAuthor = "";
        parse();

        // set image names
		mTempImage1 = rw.GetDestinationDir() + "img1.jpg";
		mTempImage2 = rw.GetDestinationDir() + "img2.jpg";		
		mImageURL1 = "https://avalanche.ws.siag.it/lawinen_v3.svc/web/resources/6100/1/0";
		mImageURL2 = "https://avalanche.ws.siag.it/lawinen_v3.svc/web/resources/6100/1/-1";
        // download images
        Utils.DownloadFromUrl(mImageURL1, mTempImage1);
		Utils.DownloadFromUrl(mImageURL2, mTempImage2);

	    mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
       
        prepareImage(mImgOnDevice);
        				
 	}
	
    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	private void prepareImage(String dest)
	{
		try
		{
			// load imgaes	 	
			BufferedImage img1 = ImageIO.read(new File(mTempImage1));
			BufferedImage img2 = ImageIO.read(new File(mTempImage2));	
			int type = img1.getType();

			// create resulting image
			BufferedImage resImg = new BufferedImage(img1.getWidth(), 
					img1.getHeight() + img2.getHeight(), type);
			
			// create canvas
			Graphics2D vCanvas = resImg.createGraphics();
			
			vCanvas.drawImage(img1, 0, 0, null);
			
			// labels
			Font font = new Font("Arial", Font.PLAIN, 24);
			vCanvas.setColor(Color.BLACK);
			vCanvas.setFont(font);
			vCanvas.drawString("Vormittag", 10, 20);
			vCanvas.drawImage(img2, 0, img1.getHeight(), null);				
			vCanvas.drawString("Nachmitag", 10, img1.getHeight()+10);
			// write back
			ImageIO.write(resImg, "jpg", new File(dest));			
		}
		catch (Exception e)
		{
			System.out.println("error " + e.getMessage());	
        }
	}
	
	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		super.characters(ch, start, length);
		builder.append(ch,start,length);
	}

	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
    public void comment(char[] ch, int start, int length) throws SAXException 
    {
    }
	
	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		super.endElement(uri, localName, name);
		
		switch (mState)
		{
			case eDate: //div class="hilite">
				if (localName.equalsIgnoreCase("p"))
				{
					
					mDate = builder.toString().replaceFirst("Lawinenlagebericht von ", "").replace("Ausgabezeitpunkt:", ",");
					mDate = mDate.substring(mDate.indexOf(' '), mDate.indexOf(',')).replace('\n', ' ');
					System.out.println("Date: " + mDate);
					SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

				    try
				    {
				    	Date reportDate = formatter.parse(mDate);
				    	Calendar now = Calendar.getInstance();			    	
				    	// truncate now to also only have date without time
				    	now.set(Calendar.HOUR_OF_DAY,0);
				    	now.set(Calendar.MINUTE,0);
				    	now.set(Calendar.SECOND, 0);
				    	now.set(Calendar.MILLISECOND, 0);
				    	Calendar reportCal = Calendar.getInstance();
				    	reportCal.setTime(reportDate);				    	
				    	while (reportCal.before(now)) 
				        {  
				    		reportCal.add(Calendar.DATE, 1);
				    		mReportAge++;  
				        }  			    	
				    }
				    catch (Exception e)
				    {
				    	
				    }
				    System.out.println("Report is " + Integer.toString(mReportAge) + " days old");
			    	//mDir = Settings.Instance().GetDataDir() + name + "/" +  df.format(date)+"/";

					mState = eState.eFindTitle;
				}
				break;
			case eTitle: // </p>
				if (localName.equalsIgnoreCase("p"))
				{
					mTitle = builder.toString();
					System.out.println("Title: " + mTitle);
					mState = eState.eFindImageLocation;
				}
				break;
			case eContent:
				if (localName.equalsIgnoreCase("h2")) 
				{
						builder.append("</strong><br>");
				}
				else if (localName.equalsIgnoreCase("span"))
				{
					builder.append("</strong><br>");					
				}
				else if (localName.equalsIgnoreCase("h3"))
				{
					builder.append("<br>");
				}
				break;
			case eRiskLevel:
				if (localName.equalsIgnoreCase("tr")) builder.append("</tr>");
				else if (localName.equalsIgnoreCase("td")) builder.append("</td>");
				else if (localName.equalsIgnoreCase("table")) 
				{
					builder.append("<br><br></table>");
					mContent = builder.toString();
					builder.setLength(0);
					mState = eState.eContent2;
				}
				break;
			case eContent2:	
				if (localName.equalsIgnoreCase("h2")) builder.append("</strong><br>");
				break;
			case eSnowLevels:
				if (localName.equalsIgnoreCase("tr")) builder.append("</tr>");
				else if (localName.equalsIgnoreCase("th")) builder.append("</th>");
				else if (localName.equalsIgnoreCase("td")) builder.append("</td>");
				else if (localName.equalsIgnoreCase("table")) 
				{
					builder.append("</table>");
					String snow = builder.toString().replace("[cm]", "");
					snow = snow.replace("Neuschnee letzte 24h", "24h"); 
					snow = snow.replace("Schneeh√∂he", "gesamt");
					snow = snow.replace("Letzter Schneefall", "");
					mContent += snow;
					builder.setLength(0);
					mState = eState.eFindTendency;
				}
				break;
			case eTendency:
				if (localName.equalsIgnoreCase("h2") || localName.equalsIgnoreCase("h3")) builder.append("</strong><br>");
				break;
			default:
				break;
		}
	}

	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
	public void startDocument() throws SAXException
	{
			super.startDocument();
			builder = new StringBuilder();
	}
		
    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
	{
			super.startElement(uri, localName, name, attributes);
			switch (mState)
			{
				case eFindDate: //div class="hilite">
					if (localName.equalsIgnoreCase("div"))  
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("hilite")) 
						{	
							builder.setLength(0);
							mState = eState.eDate;
						}
					}
					break;
				case eFindTitle: //<p class="headline">
					if (localName.equalsIgnoreCase("p"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("headline"))
						{
							builder.setLength(0);
							mState = eState.eTitle;
						}
					}
					break;
				case eFindImageLocation: // img
					if (localName.equalsIgnoreCase("img"))
					{
						String attr = attributes.getValue("src");
						if (attr != null)
						{
							//mBitmapLocation = attr.substring(0, attr.lastIndexOf('=')+1) + Integer.toString(mReportAge-1);
							System.out.println("bitmap "+ mBitmapLocation);
							builder.setLength(0);
							mState = eState.eFindContent;
						}
					}
					break;
				case eFindContent: //<div class="details">
					if (localName.equalsIgnoreCase("div"))  
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equalsIgnoreCase("details"))
						{	
							builder.setLength(0);
							mState = eState.eContent;
						}
					}
					break;
				case eContent:					
					if (localName.equalsIgnoreCase("h2")) builder.append("<strong>");
					else if (localName.equalsIgnoreCase("table"))
					{
						builder.append("<table>");
						mState = eState.eRiskLevel;
					}

					break;
				case eRiskLevel: // until table end    
					if (localName.equalsIgnoreCase("tr")) builder.append("<tr>");
					else if (localName.equalsIgnoreCase("td")) 
					{
						//class='risk level-2'
						String attr = attributes.getValue("class");
						if (attr != null) 
						{	
							if (attr.equals("risk level-1"))
								builder.append("<td bgcolor=\"#a8d14f\">");
							else if (attr.equals("risk level-2")) 
								builder.append("<td bgcolor=\"#ffff00\">");
							else if (attr.equals("risk level-3")) 
								builder.append("<td bgcolor=\"#ff9400\">");
							else if (attr.equals("risk level-4")) 
								builder.append("<td bgcolor=\"#ff0000\">");
							else if (attr.equals("risk level-5")) 
								builder.append("<td bgcolor=\"#000000\">");
						}
						else 
							builder.append("<td>");
					}
					break;			
				case eContent2:
					if (localName.equalsIgnoreCase("table")) 
					{
						mContent+=builder.toString();
						builder.setLength(0);
						builder.append("<table>");
						mState = eState.eSnowLevels;
					}
					else if (localName.equalsIgnoreCase("h2")) builder.append("<br><strong>");
					break;
				case eSnowLevels: // until table end    
					if (localName.equalsIgnoreCase("th")) builder.append("<th>");
					else if (localName.equalsIgnoreCase("tr")) builder.append("<tr>");
					else if (localName.equalsIgnoreCase("td")) builder.append("<td>");
					break;
				case eFindTendency:
					if (localName.equalsIgnoreCase("div"))
					{
						String attr = attributes.getValue("id");
						if ((attr != null) && attr.equals("tendency")) 
						{
							builder.setLength(0);
							builder.append("<br><strong>");
							mState = eState.eTendency;
						}
					}
					break;
				case eTendency:
					if (localName.equalsIgnoreCase("h3")) 
					{
						builder.append("<strong>");
					}
					else if (localName.equalsIgnoreCase("table"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("avalanches-forecast")) 
							System.out.println("parser" + builder.toString());
						mContent += builder.toString().replace("‚Ä∫ Bergwetter", "");
						builder.setLength(0);
						mState = eState.eNada3;
					}
					break;
				default:
					break;
			}
	}
}

