package src.net.eckschi.lawineserver;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


//http://warndienste.cnv.at/dibos/lawine/bilder/aspect/aspect11111111.gif
//<h2 class="ueberschrift_h2">

public class FetchVorarlberg extends AvalancheReport 
{
	private enum eState 
	{
		eFindDate,
		eDate,
		eFindTitle,
		eTitle,
		eFindImage1,
		eFindImage2,
		eFindContent,
		eContent,
		eNada3
	};
	
	private eState mState = eState.eFindDate; 
	private int mContentCnt = 0;
	private String mImageURL1;
	private String mImageURL2;
	private String mTempImage1;
	private String mTempImage2;
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchVorarlberg(ReportWriter rw)
	{
		super();
		mUrlStr = "http://warndienste.cnv.at/dibos/lawine/";
		mEncoding = "ISO-8859-1";
        mCopyright = "© Landeswarnzentrale Vorarlberg";

        // set image names
		mTempImage1 = rw.GetDestinationDir() + "img1.png";
		mTempImage2 = rw.GetDestinationDir() + "img2.png";		
		mImgOnDevice = rw.GetDestinationDir() + "map.gif";
		
        // parse
        parse();
		
        // download images
        Utils.DownloadFromUrl(mImageURL1, mTempImage1);
		Utils.DownloadFromUrl(mImageURL2, mTempImage2);

        
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
			BufferedImage resImg = new BufferedImage(img1.getWidth()+ img2.getWidth(), 
					Math.max(img1.getHeight(), img2.getHeight()), type);
			
			// create canvas
			Graphics2D vCanvas = resImg.createGraphics();
			
			vCanvas.drawImage(img1, 0, 0, null);
			
			// labels
			Font font = new Font("Arial", Font.PLAIN, 24);
			vCanvas.setColor(Color.BLACK);
			vCanvas.setFont(font);
			vCanvas.drawString("Vormittag", 10, 20);
			if (mImageURL2.contains("aspect"))
			{
				vCanvas.drawImage(img2, img1.getWidth(), 20, null);
				vCanvas.drawString("Exposition", img1.getWidth()+10, 20);
				System.out.println("exposition");
			}
			else
			{
				vCanvas.drawImage(img2, img1.getWidth(), 0, null);				
				vCanvas.drawString("Nachmitag", img1.getWidth()+10, 20);
				System.out.println("nachmittag");
			}
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
				if (localName.equalsIgnoreCase("H2"))
				{
					mDate = builder.toString();
					mDate = mDate.substring(mDate.indexOf(",")+2);
					System.out.println("Date: "+mDate.toString());
					mState = eState.eFindTitle;
				}
				break;
			case eTitle:
				if (localName.equalsIgnoreCase("p"))
				{
					mTitle = builder.toString();
					System.out.println(mTitle);
					mState = eState.eFindImage1;
				}
				break;
			case eContent:
				if (localName.equalsIgnoreCase("p"))
				{
					builder.append("</strong><br>");
				}
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
				case eFindDate: //<h2 class="ueberschrift_h2">
					if (localName.equalsIgnoreCase("H2"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("ueberschrift_h2")) 
						{
							builder.setLength(0);
							mState = eState.eDate;
						}
					}
					break;
				case eFindTitle:
					if (localName.equalsIgnoreCase("p"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("news_anreisser")) 
						{
							builder.setLength(0);
							mState = eState.eTitle;
						}
					}
					break;
				case eFindImage1:
					if (localName.equalsIgnoreCase("img"))
					{
						mImageURL1 = "http://warndienste.cnv.at/dibos/lawine/" + attributes.getValue("src");
						System.out.println("first image at: " + mImageURL1);
						mState = eState.eFindImage2;
					}
					break;
				case eFindImage2:
					if (localName.equalsIgnoreCase("img"))
					{
						mImageURL2 = "http://warndienste.cnv.at/dibos/lawine/" + attributes.getValue("src");
						System.out.println("second image at: " + mImageURL2);
						mState = eState.eFindContent;
					}
					break;
				case eFindContent:
					if (localName.equalsIgnoreCase("div"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && (attr.equalsIgnoreCase("item_100")))
						{
							builder.setLength(0);
							mContentCnt = 0;
							mState = eState.eContent;
						}
					}
					break;
				case eContent:
					if (localName.equalsIgnoreCase("div"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && (attr.equalsIgnoreCase("item_100")))
						{
							mContentCnt++;
							if (mContentCnt == 4)
							{
								mContent = builder.toString();
								int sep = mContent.lastIndexOf('.')+1;
								mAuthor = mContent.substring(sep);
								mContent = mContent.substring(0, sep);
								System.out.println("Content: " + mContent);
								System.out.println("Author: " + mAuthor);
								mState = eState.eNada3;
							}
						}
					}
					else if (localName.equalsIgnoreCase("p"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && (attr.equalsIgnoreCase("news_anreisser")))
						{
							builder.append("<br><strong>");
						}
					}
					break;					
			}
	}
}

