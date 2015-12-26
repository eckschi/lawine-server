package src.net.eckschi.lawineserver;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class FetchVeneto extends AvalancheReport 
{
	private enum eState 
	{
		eFindDate,
		eDate,
		eFindTitle,
		eTitle,
		eFindContent,
		eContent,
		eFindOutlook,
		eOutlook,
		eFindContent2,
		eContent2,
		eFindAuthor,
		eAuthor,
		eNada3
	};
	
	private eState mState = eState.eFindDate; 
	private String mImageURL1;
	private String mImageURL2;
	private String mImageURL3;
	private String mTempImage1;
	private String mTempImage2;
	private String mTempImage3;
	private String mOutlook;
	private boolean mOutlookCont = false;
	private int mOutlookIdx;
	
	//-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchVeneto(ReportWriter rw)
	{
		super();
		// search for the id of the report in the main page
		mUrlStr = "http://www.arpa.veneto.it/bollettini/htm/dolomiti_neve_e_valanghe.asp";
        mImageURL1 = "http://www.arpa.veneto.it/upload_arabba/bollettini_meteo/dolomitineve/valanghe.jpg";
        mImageURL2 = "http://www.arpa.veneto.it/upload_arabba/bollettini_meteo/dolomitineve/pendii.jpg";	
        mImageURL3 = "http://www.arpa.veneto.it/upload_arabba/bollettini_meteo/dolomitineve/profili.jpg";
		mEncoding = "ISO-8859-1";
		mCopyright = "©ARPA Veneto - Tutti i diritti riservati - P.IVA 03382700288";
		mAuthor = "(machine-translated text)";
        parse();
		mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
		
        // set local file names
		mTempImage1 = rw.GetDestinationDir() + "img1.png";
		mTempImage2 = rw.GetDestinationDir() + "img2.png";		
		mTempImage3 = rw.GetDestinationDir() + "img3.png";		
		mImgOnDevice = rw.GetDestinationDir() + "map.gif";
        
        // download images
        Utils.DownloadFromUrl(mImageURL1, mTempImage1);
		Utils.DownloadFromUrl(mImageURL2, mTempImage2);
		Utils.DownloadFromUrl(mImageURL3, mTempImage3);
		
		
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
			BufferedImage img3 = ImageIO.read(new File(mTempImage3));
					
		    int type = img1.getType();

		    double scale = 2.0;
			double scale2 = 2.5;
			double scale3 = 2.8;
			// create resulting image make it a bit smaller
			int w1 = (int)(img1.getWidth()/scale);
			int h1 = (int)(img1.getHeight()/scale);
		    int w2 = (int)(img2.getWidth()/scale2);
		    int h2 = (int)(img2.getHeight()/scale2);
		    int w3 = (int)(img2.getWidth()/scale3);
		    int h3 = (int)(img2.getHeight()/scale3);
			BufferedImage resImg = new BufferedImage(w1, h1 + h2, type);
			
			// create canvas
			Graphics2D vCanvas = resImg.createGraphics();
		    vCanvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			vCanvas.setBackground(Color.WHITE);
			vCanvas.clearRect(0, 0, w1, h1+h2);
			vCanvas.drawImage(img1, 0, 0, w1, h1, null);		
	        vCanvas.drawImage(img2, 0, h1, w2, h2, null);
	        vCanvas.drawImage(img3, w2, h1, w3, h3, null);
			
			// write back
			ImageIO.write(resImg, "gif", new File(dest));			
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
			case eDate: 
				if (localName.equalsIgnoreCase("h2"))
				{
					
					mDate = Utils.TranslateItalian(builder.toString());
					System.out.println("Date: " + mDate);
					mState = eState.eFindTitle;
				}
				break;
			case eTitle: 
				if (localName.equalsIgnoreCase("strong"))
				{
					mTitle = Utils.TranslateItalian(builder.toString().replace("\n", ""));
					System.out.println("Title: " + mTitle);
					mState = eState.eFindContent;
				}
				break;
			case eContent:
				if (localName.equalsIgnoreCase("p"))
				{
					mContent = Utils.TranslateItalian(builder.toString());
					mState = eState.eFindOutlook;
				}
				break;
			case eOutlook:
				if (localName.equalsIgnoreCase("table"))
				{
					mOutlook += "</table>";
					mState = eState.eFindContent2;
				}
				else if (localName.equalsIgnoreCase("tr"))
				{
					mOutlook += "</tr>";
					mOutlookCont = true;
				}
				else if (localName.equalsIgnoreCase("th"))
				{
					mOutlook += builder.toString() + "</td>";

				}
				else if (localName.equalsIgnoreCase("td") && (mOutlookIdx != 1))
				{
					mOutlook += builder.toString() + "</td>";
				}				
				break;
			case eFindContent2: // h2
				if (localName.equalsIgnoreCase("h2"))  
				{
					builder.setLength(0);
					mState = eState.eContent2;
				}		
				break;

			case eContent2:
				if (localName.equalsIgnoreCase("p"))
				{
					mContent = mContent + "<br><strong>Forecast</strong><br>" + Utils.TranslateItalian(builder.toString());
					System.out.println("Content: " + mContent);
					mContent += mOutlook;
					mState = eState.eNada3;
				}
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
				case eFindDate: // h2 style="border:0px;"
					if (localName.equalsIgnoreCase("h2"))  
					{
						String attr = attributes.getValue("style");
						if ((attr != null) && attr.equals("border:0px;")) 
						{	
							builder.setLength(0);
							mState = eState.eDate;
						}
					}
					break;
				case eFindTitle: //<strong>
					if (localName.equalsIgnoreCase("strong"))
					{
						builder.setLength(0);
						mState = eState.eTitle;
					}
					break;
				case eFindContent: //<p">
					if (localName.equalsIgnoreCase("p"))  
					{
						System.out.println("content found");
						builder.setLength(0);
						mState = eState.eContent;
					}		
					break;	
				case eFindOutlook:
					if (localName.equalsIgnoreCase("div"))  
					{
						String attr = attributes.getValue("style");
						if ((attr != null) && attr.equals("float:right; width:50%; text-align:center; margin-left:5px;margin-top:2em;"))
						{
							System.out.println("outlook found");
							builder.setLength(0);
							mState = eState.eOutlook;
						}
					}
					break;
				case eOutlook:
					if (localName.equalsIgnoreCase("img"))
					{
						String attr = attributes.getValue("src");
						if (mOutlookIdx == 5)
							mOutlook += "<img width=\"80\" src=\"http://www.arpa.veneto.it" + attr + "\" />";
						else
							mOutlook += "<img width=\"30\" src=\"http://www.arpa.veneto.it" + attr + "\" />";
					}
					else if (localName.equalsIgnoreCase("table"))
					{
						mOutlook += "<table><tr><th>day</th><th>weather</th><th>new snow</th><th>danger</th>";
					} 
					else if (localName.equalsIgnoreCase("tr"))
					{
						mOutlook += "<tr>";
						mOutlookIdx = 0;
					}
					else if (mOutlookCont && localName.equalsIgnoreCase("th"))
					{
						mOutlook += "<td>";
						builder.setLength(0);
						mOutlookIdx++;
					}
					else if (localName.equalsIgnoreCase("td"))
					{
						if (mOutlookIdx != 1)
						{
							mOutlook += "<td>";
							builder.setLength(0);
						}
						mOutlookIdx++;
					}
						
					break;
			}
	}
}
