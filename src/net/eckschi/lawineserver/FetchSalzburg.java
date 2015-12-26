package src.net.eckschi.lawineserver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FetchSalzburg extends AvalancheReport
{
	private enum eState 
	{
		eFindDate,
		eDate,
		eFindUpdate,
		eUpdate,
		eFindImageUrl1,
		eFindImageUrl2, 
		eFindTitle,
		eTitle,
		eFindContent,
		eContent,
		eAuthor,
		eNada1
		
	};
	
	private eState mState = eState.eFindDate; 

   
	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------	
	public FetchSalzburg(ReportWriter rw)
	{
		super();
		mUrlStr = "http://www.lawine.salzburg.at/lagebericht.asp";
		mEncoding = "UTF-8";
		mCopyright = "© Lawinenwarndienst Salzburg";
		mImgOnDevice = rw.GetDestinationDir() + "map.gif";
        parse();
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
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		super.endElement(uri, localName, name);
		
		switch (mState)
		{
			// date
			case eDate:
				if (localName.equalsIgnoreCase("div"))
				{
					mDate = builder.toString().replace("Lagebericht", "");
					mState = eState.eFindUpdate;
				}
				break;
			case eUpdate:
				if (localName.equalsIgnoreCase("div"))
				{
					String update = builder.toString();
					mDate += " " + update.substring(0,update.indexOf('(')).replace("update", "").replace("heute","");
					System.out.println("Date:" + mDate);
					mAuthor = update.substring(update.indexOf('(')+1, update.indexOf(')'));
					System.out.println("author:" + mAuthor);
					mState = eState.eFindTitle;
				}
				break;
			case eTitle: //<h1>					
				if (localName.equalsIgnoreCase("h1"))  
				{
					mTitle = builder.toString();
					builder.setLength(0);
					System.out.println("Title: " + mTitle);
					mState = eState.eFindImageUrl1;
				}
				break;	
			case eFindContent: //</span>
				if (localName.equalsIgnoreCase("span"))
				{
					builder.setLength(0); 
					mState = eState.eContent; 
				}
				break;
			case eContent: 
				if (localName.equalsIgnoreCase("h2"))
				{
					builder.append("</strong><br>");
				}
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
				case eFindDate: //validfor
					if (localName.equalsIgnoreCase("div"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("validfor")) 
						{
							builder.setLength(0); 
							mState = eState.eDate;
						}
					}
					break;
				case eFindUpdate: //<div class="lastupdate">
					if (localName.equalsIgnoreCase("div"))
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("lastupdate")) 
						{
							builder.setLength(0); 
							mState = eState.eUpdate;
						}
					}
					break;
				case eFindTitle: //<h1
					if (localName.equalsIgnoreCase("h1"))
					{	
						builder.setLength(0); 
						mState = eState.eTitle; 
					}
					break;
				case eFindImageUrl1: //img  
					if (localName.equalsIgnoreCase("img"))
					{
						mBitmapLocation = "http://www.lawine.salzburg.at" + attributes.getValue("src");
						System.out.println("image1 " + mBitmapLocation);
						mState = eState.eFindImageUrl2;
					}
					break;
				case eFindImageUrl2: //img  
					if (localName.equalsIgnoreCase("img"))
					{
						String img2 = "http://www.lawine.salzburg.at" + attributes.getValue("src");
						System.out.println("image2 " + img2);
						mState = eState.eFindContent;
					}
					break;
				case eContent: // ends with <script>
					if (localName.equalsIgnoreCase("script"))
					{
						mContent = builder.toString();
						if (mContent.indexOf("Vormittag") == 0)
								mContent = mContent.substring(9);
						System.out.println("content " + mContent);
						mState = eState.eNada1;
					}
					else if (localName.equalsIgnoreCase("h2"))
					{
						builder.append("<br><strong>");
					}
					break;
				default: 
					break;
			}
	}
}
