package src.net.eckschi.lawineserver;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class FetchTyrol extends AvalancheReport 
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
		eNada3
	};
	
	private eState mState = eState.eNada0; 
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchTyrol(ReportWriter rw)
	{
		super();
		//mUrlStr = "http://lwd.tirol.gv.at/produkte/llb/_today/LLBTirolRss.xml";
		mUrlStr = "http://lawine.tirol.gv.at/";
		mEncoding = "UTF-8";
        mImgOnDevice = rw.GetDestinationDir() + "map.jpg";
        mCopyright = "LWD Tirol";
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
    public void comment(char[] ch, int start, int length) throws SAXException 
    {
		String comment = String.valueOf(ch, start, length);
		if ((mState==eState.eNada0) && (comment.contains("uid:93057")))
		{	
			System.out.println("FetchTirol got signal!!");
			mState = eState.eDate;
		}
    }
	
	//------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		super.endElement(uri, localName, name);
		
		switch (mState)
		{
//			// author
//			case eAuthor:
//				if (localName.equalsIgnoreCase("STRONG"))
//				{
//					//Log.i("Author", builder.toString());
//					mAuthor = builder.toString();
//					mState = eState.eNada1;
//				}
//				break;
//				
//			// date
			case eDate:
				if (localName.equalsIgnoreCase("H3"))
				{
					System.out.println("Date" + builder.toString());
					mDate = builder.toString();
					mState = eState.eImageSrc;
				}
				break;

			// title
			case eTitle:
				if (localName.equalsIgnoreCase("H1"))
				{
					mTitle = builder.toString();
					System.out.println("Title" + mTitle);
					mState = eState.eNada2;
				}
				break;
			
			// content
			case eContent:
				if (localName.equalsIgnoreCase("H2")) builder.append("</strong><br>");
				else if (localName.equalsIgnoreCase("DIV"))
				{
					mContent = builder.toString();
					int autIdx = mContent.indexOf("[Autor:");
					mAuthor = mContent.substring(autIdx+8, mContent.length()-3);
					System.out.println("autor" + mAuthor);
					mContent = mContent.substring(0, autIdx);
					System.out.println("Content" + mContent);
					mState = eState.eNada3;
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
//		Log.i("start", localName);
			super.startElement(uri, localName, name, attributes);
			switch (mState)
			{
				case eDate:
					if (localName.equalsIgnoreCase("H3"))
					{
						builder.setLength(0); 					
					}
					break;
				case eImageSrc:
					if (localName.equalsIgnoreCase("IMG"))
					{
						mBitmapLocation = attributes.getValue("src");
						System.out.println("image" + mBitmapLocation);
						mState = eState.eNada1;
					}
					break;
				case eNada1:
					if (localName.equalsIgnoreCase("H1"))  
					{
						builder.setLength(0); 
						mState = eState.eTitle;
					}
					break;
				case eNada2:
					if (localName.equalsIgnoreCase("H2"))  
					{
						builder.setLength(0);
						builder.append("<strong>");
						mState = eState.eContent;
					}
					break;
				case eContent:
					if (localName.equalsIgnoreCase("H2")) builder.append("<br><strong>");
					break;
			}
	}
}
