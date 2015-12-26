package src.net.eckschi.lawineserver;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class FetchStyriaEn extends AvalancheReport 
{
	private enum eState 
	{
		eNada0,
		eDate,
		eImageSrc,	
		eNada1,
		eTitle,
		eNada2,
		eGeneralGrade,
		eGradeTable,
		eContent,
		eNada3,
		eAuthor,
		eNada4
	};
	
	private eState mState = eState.eNada0;
	private int mCnt=0;
	private int mTopicCnt = 0;
	private boolean mTopic=false;
	
	//
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchStyriaEn(ReportWriter rw)
	{
		super();
		mUrlStr = "http://www.lawine-steiermark.at/lage.php";
		mEncoding = "ISO-8859-1";
        mBitmapLocation = "http://www.lawine-steiermark.at/lage.gif";
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
				if (localName.equalsIgnoreCase("TD"))
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
					mTitle = Utils.TranslateEnglish(builder.toString());
					mState = eState.eNada2;
				}
				break;
			case eGeneralGrade:
				if (localName.equalsIgnoreCase("TR") && (mCnt++ == 1))
				{
					builder.append("</b><br/>");
					mState = eState.eGradeTable;
					mCnt = 0;
				}
				break;
			case eGradeTable:
				if (localName.equalsIgnoreCase("TD")) 
				{
					if (mCnt++ == 1)
					{
						builder.append("<br/>");
					}
				}
				else if (localName.equalsIgnoreCase("TR")) 
				{
					builder.append("<br/>");
					mCnt = 0;
				}
				if (localName.equalsIgnoreCase("TABLE"))
				{
					mCnt = 0;
					mState = eState.eContent;
				}
				break;
			case eContent:
				if ((mTopicCnt==4) && localName.equalsIgnoreCase("TR") && (mCnt++ == 1))
				{
					mContent = Utils.TranslateEnglish(builder.toString());
					System.out.println(mContent);
					mState = eState.eNada3;
					mCnt = 0;
				}
				else if (mTopic && localName.equalsIgnoreCase("TR"))
				{	
					mTopic = false;
					builder.append("</b><br/>");
				}
				break;
			case eAuthor:
				if (localName.equalsIgnoreCase("TD"))
				{
					mAuthor = builder.toString();
					mState = eState.eNada4;
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
		//Log.i("start", localName);
			super.startElement(uri, localName, name, attributes);
			switch (mState)
			{
				case eNada0:
					if (localName.equalsIgnoreCase("TABLE"))
					{
						mState = eState.eDate;
						builder.setLength(0);
					}
					break;
				case eNada1:
					if (localName.equalsIgnoreCase("TABLE"))
					{
						mState = eState.eTitle;
						builder.setLength(0);
					}
					break;
				case eNada2:
					if (localName.equalsIgnoreCase("TABLE"))
					{
						System.out.println("parser content started");
						mState = eState.eGeneralGrade;
						builder.setLength(0);
						builder.append("<b>");
					}
					break;
				case eContent:
					if (localName.equalsIgnoreCase("FONT"))
					{
						mTopic = true;
						if (mTopicCnt++ != 0) builder.append("<br/>"); 
						builder.append("<b>");
					}
					break;
				case eNada3:
					if (localName.equalsIgnoreCase("BR") && (mCnt++ == 1))
					{
						mState = eState.eAuthor;
						builder.setLength(0);
					}
					break;
				default:
					break;
			}
	}
}
