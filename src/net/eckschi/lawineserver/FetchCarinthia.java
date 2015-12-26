package src.net.eckschi.lawineserver;



import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FetchCarinthia extends AvalancheReport
{
	private enum eState 
	{
		eFindTitle,
		eTitle,
		eFindImage,
		eFindDate,
		eDate,
		eFindContent,
		eContent,
		eNada3
	};

	private eState mState = eState.eFindTitle; 
	//
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchCarinthia(ReportWriter rw)
	{
		super();
		mUrlStr = "http://www.lawinenwarndienst.ktn.gv.at";		   
		mEncoding = "UTF-8";
		mAuthor = "";
        parse();
        mDate = "";
		
        mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
        mCopyright = "© Lawinenwarndienst KŠrnten";
        Utils.DownloadFromUrl(mBitmapLocation, mImgOnDevice);
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
//		String comment = String.valueOf(ch, start, length);
    }
	
	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		super.endElement(uri, localName, name);
		
		switch (mState)
		{
			case eTitle:
			if (localName.equalsIgnoreCase("div"))
			{
				mTitle = builder.toString();
				mState = eState.eFindImage;
			}
			break;
			case eDate:
				if (localName.equalsIgnoreCase("div"))
				{
					mDate = builder.toString();
				}
				break;			
			case eContent:
				if (localName.equalsIgnoreCase("div"))
				{
					mContent = builder.toString();
					mState = eState.eNada3;
				}
				else if (localName.equalsIgnoreCase("font"))
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
				case eFindTitle:
					if (localName.equalsIgnoreCase("div"))
					{
						String attr = attributes.getValue("style");
						if ((attr != null) && attr.equalsIgnoreCase("TEXT-ALIGN: center")) 
						{	
							builder.setLength(0);
							mState = eState.eTitle;
						}
					}					
					break;
				case eFindImage:
					if (localName.equalsIgnoreCase("img"))
					{
						String attr = attributes.getValue("src");
						if ((attr != null)) 
						{
							mBitmapLocation = "http://www.lawinenwarndienst.ktn.gv.at/"+attr;
							mState = eState.eFindContent;
						}
					}
					break;
				case eFindDate:
					if (localName.equalsIgnoreCase("DIV"))
					{
						String attr = attributes.getValue("id");
						if ((attr != null) && attr.equals("Tag")) 
						{
							mState = eState.eDate;
						}
					}
					break;
				case eFindContent:
					if (localName.equalsIgnoreCase("font"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("fontbold")) 
						{
							builder.setLength(0);
							builder.append("<strong>");
							mState = eState.eContent;
						}
					}
					break;
/*				case eContent:
					if (localName.equalsIgnoreCase("a"))
					{
						mContent = builder.toString();
						mState = eState.eNada3;
					}
					else if (localName.equalsIgnoreCase("th"))
					{
						builder.append("<br><strong>");
					}
					else if (localName.equalsIgnoreCase("form"))
					{
						
						
					}
					break;
*/
			}
	}
}


