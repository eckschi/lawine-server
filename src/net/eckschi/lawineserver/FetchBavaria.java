package src.net.eckschi.lawineserver;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class FetchBavaria extends AvalancheReport 
{
	private enum eState 
	{
		eFindReportId,
		eNada1,
		eFindDate,
		eDate,
		eFindTitle,
		eTitle,
		eFindLevels,
		eLevels,
		eFindImageLocation,
		eFindContent,
		eContent,
		eFindAuthor,
		eAuthor,
		eNada3
	};
	
	private eState mState = eState.eFindReportId; 
	private int mLevels[] = new int[12];
	private int mLevelIdx = 0;
	private String mWindroseLocation;
	private String mReportId;
	//-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchBavaria(ReportWriter rw)
	{
		super();
		// search for the id of the report in the main page
		mUrlStr = "http://www.lawinenwarndienst-bayern.de/index.php";
        parse();
        mState = eState.eFindDate;
        // then load and parse page with that particular id
		mUrlStr = "http://www.lawinenwarndienst-bayern.de/lagebericht/index.php?ID=" + mReportId;
		mEncoding = "ISO-8859-1";
		mCopyright = "© 2012 Lawinenwarndienst Bayern";
		mAuthor = "";
        parse();
		mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
		mWindroseLocation = rw.GetDestinationDir() + "bavWindrose.gif";
		Utils.DownloadFromUrl("http://www.lawinenwarndienst-bayern.de/lagebericht/sektor.php?ID=" + mReportId, mWindroseLocation);
		prepareImage(mImgOnDevice);
	}
	
    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	private void prepareImage(String dest)
	{
		try
		{
			// merge
			BufferedImage origImg = ImageIO.read(getClass().getResourceAsStream("/resources/bavaria/relief520x183_start.gif"));
			BufferedImage img = new BufferedImage(origImg.getWidth(), origImg.getHeight(), BufferedImage.TYPE_INT_RGB); 
			Graphics2D vCanvas = img.createGraphics();
			vCanvas.drawImage(origImg, 0, 0, null);

			// levels
			for (int j=0; j<2; j++)
			{
				for (int i=0; i<6; i++)
				{
//					System.out.println("blit " + Integer.toString(i+6*j) + " " + Integer.toString(mLevels[i+6*j]));
					BufferedImage testBmp = ImageIO.read(getClass().getResourceAsStream("/resources/carinthia/" + Integer.toString(mLevels[i+6*j]) + ".gif"));
					if (testBmp != null)
						vCanvas.drawImage(testBmp, 60+75*i, 40+50*j, null);
					else System.out.println("not blitting" + mLevels[i]);
				}
			}

			// windrose
			BufferedImage windroseBmp = ImageIO.read(new File(mWindroseLocation)); 
			Image windroseTransparent = Utils.makeColorTransparent(windroseBmp, new Color(255,255,255)).getScaledInstance(60, 60, Image.SCALE_SMOOTH);
			vCanvas.drawImage(windroseTransparent, 0, 25, null);
			// write back
			ImageIO.write(img, "jpeg", new File(dest));
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
				if (localName.equalsIgnoreCase("td"))
				{
					
					mDate = builder.toString();
					mDate = mDate.substring(mDate.indexOf(',')+2);
					System.out.println("Date: " + mDate);
					mState = eState.eFindTitle;
				}
				break;
			case eTitle: // </b>
				if (localName.equalsIgnoreCase("b"))
				{
					mTitle = builder.toString().replace("\n", "");
					System.out.println("Title: " + mTitle);
					mState = eState.eFindLevels;
				}
				break;
			case eLevels: // levels end when table ends
				if (localName.equalsIgnoreCase("table"))
				{
					mState = eState.eFindContent;
				}
				break;
			case eContent:
				if (localName.equalsIgnoreCase("table")) 
				{
					mContent = builder.toString();
					System.out.print("Content:" + mContent + "\n");
					mState = eState.eNada3;
				}
				else if (localName.equalsIgnoreCase("b"))
				{
					builder.append("</strong>");					
				}
				break;
			default: break;	
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
				case eFindReportId: //<a href="lagebericht/index.php?ID=2050">...zum Lawinenlagebericht</a>
					if (localName.equalsIgnoreCase("a"))  
					{
						String attr = attributes.getValue("href");
						if ((attr != null) && attr.contains("lagebericht/index.php?ID=")) 
						{
							mReportId = attr.substring(attr.lastIndexOf('=')+1);
							System.out.println("Report ID is " + mReportId);
							mState = eState.eNada1;
						}
					}
					break;
				case eFindDate: //div class="hilite">
					if (localName.equalsIgnoreCase("td"))  
					{
						//String attr = attributes.getValue("height");
						//if ((attr != null) && attr.equals("22")) 
						String attr = attributes.getValue("style");
						if ((attr != null) && attr.equals("background-color:#F5F5F5; border:0px; border-bottom: 1px; border-color: #0A58BF; border-style: solid; padding-left:5px; padding-right:5px; padding-top:5px; padding-bottom:5px")) 
						{	
							builder.setLength(0);
							mState = eState.eDate;
						}
					}
					break;
				case eFindTitle: //<td class="text_medium" 
					if (localName.equalsIgnoreCase("td"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("text_medium"))
						{
							builder.setLength(0);
							mState = eState.eTitle;
						}
					}
					break;
				case eFindLevels:
					if (localName.equalsIgnoreCase("td"))
					{
						String attr = attributes.getValue("id");
						if ((attr != null) && attr.equals("h1"))
						{
							mState = eState.eLevels;
						}
					}
					break;
				case eLevels:
					if (localName.equalsIgnoreCase("img"))
					{
						String levelstr = attributes.getValue("src");
						levelstr = levelstr.substring(levelstr.lastIndexOf('_')+1, levelstr.lastIndexOf('.'));
						int level = Integer.parseInt(levelstr);
						mLevels[mLevelIdx++] = level;
						//System.out.println(Integer.toString(mLevels[mLevelIdx-1]));
					}
					break;
				case eFindContent: //<p style="padding-right:10px;">
					if (localName.equalsIgnoreCase("p"))  
					{
						String attr = attributes.getValue("style");
						if ((attr != null) && attr.equalsIgnoreCase("padding-right:10px;"))
						{	
							System.out.println("content found");
							builder.setLength(0);
							mState = eState.eContent;
						}
					}		
					break;
				case eContent:					
				    if (localName.equalsIgnoreCase("b"))
					{
						builder.append("<strong>");
					}
					else if (localName.equalsIgnoreCase("br"))
					{
						builder.append("<br>");
					}			

					break;
				default: 
					break;
			}
	}
}
