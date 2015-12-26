package src.net.eckschi.lawineserver;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class FetchStyria extends AvalancheReport 
{
	private enum eState 
	{
		eNada0,
		eDate,
		eImageSrc,	
		eNada1,
		eTitle,
		eNada2,
		eContent,
		eNada3,
		eAuthor,
	};
	
	private eState mState = eState.eNada0;
	private boolean mTopic=false;
	
	//
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchStyria(ReportWriter rw)
	{
		super();
		mUrlStr = "http://www.lawine-steiermark.at/content/lagebericht/lagebericht.php";
		mEncoding = "ISO-8859-1";
        mBitmapLocation = "http://www.lawine-steiermark.at/content/lagebericht/stmk_web.png";
		String tempImage = rw.GetDestinationDir() + "map.gif";
        mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
		mCopyright = "Lawinenwarndienst Steiermark";
        parse();
		Utils.DownloadFromUrl(mBitmapLocation, tempImage);
		prepareImage(tempImage, mImgOnDevice);
	}
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	private void prepareImage(String source, String dest)
	{
		try
		{
			BufferedImage img = ImageIO.read(new File(source));
			if (img == null) System.out.println("something wicked happend");

			int xoffs = 15;
			int yoffs = 0;
			int w = img.getWidth()-30;
			int h = img.getHeight();
			
			// stupid subimage isn't working propperly, ignores offsets
			BufferedImage img2 = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
			for (int i = 0; i<h; i++)
			{
				for (int j=0; j<w; j++)
				{
					int rgb = img.getRGB(xoffs+j, yoffs+i);
					img2.setRGB(j, i, rgb);
				}
			}		

			// write back
			ImageIO.write(img2, "jpg", new File(dest));			
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
				if (localName.equalsIgnoreCase("div"))
				{
					mDate = builder.toString();
					mDate = mDate.substring(mDate.indexOf(",")+5);
// 				    SimpleDateFormat sdf = new SimpleDateFormat("dd.MMM.yyyy", Locale.GERMAN);
//					Date d = sdf.parse(builder.toString());
//					Log.i("date", d.toString());
					mState = eState.eNada1;
				}
				break;
			case eTitle:
				if (localName.equalsIgnoreCase("TD"))
				{
					mTitle = builder.toString();
					mState = eState.eNada2;
				}
				break;
			case eContent:
				if (localName.equalsIgnoreCase("table"))
				{
					mContent = builder.toString();
					System.out.println(mContent);
					mState = eState.eNada3;
				}
				else if (mTopic && localName.equalsIgnoreCase("b"))
				{	
					mTopic = false;
					builder.append("</b><br>");
				}
				break;
			case eAuthor:
				if (localName.equalsIgnoreCase("TD"))
				{
					mAuthor = builder.toString();
					//mState = eState.eNada4;
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
				case eNada0: // find date
					if (localName.equalsIgnoreCase("div") && attributes.getValue("id") != null && attributes.getValue("id").equalsIgnoreCase("datum"))
					{
						mState = eState.eDate;
						builder.setLength(0);
					}
					break;
				case eNada1: // find title
					if (localName.equalsIgnoreCase("td") && attributes.getValue("class") != null && attributes.getValue("class").equalsIgnoreCase("td6") )
					{
						mState = eState.eTitle;
						builder.setLength(0);
					}
					break;
				case eNada2:
					if (localName.equalsIgnoreCase("td"))
					{
						System.out.println("parser content started");
						mState = eState.eContent;
						builder.setLength(0);
						//builder.append("<b>");
					}
					break;
				case eContent:
					if (localName.equalsIgnoreCase("b"))
					{
						mTopic = true;
					    builder.append("<br>"); 
						builder.append("<b>");
					}
					break;
				default:
					break;
			}
	}
}
