package src.net.eckschi.lawineserver;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;


public class FetchTrentino extends AvalancheReport 
{
	private enum eState 
	{
		eFindDate,
		eDate,
		eFindTitle,
		eTitle,
		eFindLevels,
		eLevels,
		eFindImageLocation,
		eFindContent,
		eContent,
		eFindContent2,
		eContent2,
		eAuthor,
		eNada3
	};
	
	private eState mState = eState.eFindDate; 
	private String mImageURL1;
	private String mImageURL2;
	private String mTempImage1;
	private String mTempImage2;
	
	//-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchTrentino(ReportWriter rw)
	{
		super();

		// search for the id of the report in the main page
		mUrlStr = "http://www.meteotrentino.it/bollettini/today/valanghe_it.xml";
        mImageURL1 = "http://www.meteotrentino.it/bollettini/today/img/valanghe/indicePericolo_IT.png";
        mImageURL2 = "http://www.meteotrentino.it/bollettini/today/img/valanghe/neveAlSuolo_IT.png";	

        // then load and parse page with that particular id
		mEncoding = "UTF-8";
		mCopyright = "© 2012 Provincia Autonoma di Trento - Meteotrentino";
		mAuthor = "(machine-translated text)";
        parse();
	
        // set local file names
		mTempImage1 = rw.GetDestinationDir() + "img1.png";
		mTempImage2 = rw.GetDestinationDir() + "img2.png";		
		mImgOnDevice = rw.GetDestinationDir() + "map.gif";
        
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

			// create resulting image make it a bit smaller
			BufferedImage resImg = new BufferedImage(Math.max(img1.getWidth(),img2.getWidth())-60, 
					img1.getHeight() + img2.getHeight(), type);
			
			// create canvas
			Graphics2D vCanvas = resImg.createGraphics();
			
			vCanvas.drawImage(img1, 0, 0, null);		
			vCanvas.drawImage(img2, 0, img1.getHeight(), null);
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
				if (localName.equalsIgnoreCase("giornoEmissione"))
				{
					try
					{
						mDate = Translate.execute(builder.toString(), Language.ITALIAN, Language.ENGLISH);
					}
					catch (Exception e)
					{
						System.out.println("lost in translation: " + e.toString());
					}
					System.out.println("Date: " + mDate);
					mState = eState.eFindTitle;
				}
				break;
			case eTitle: 
				if (localName.equalsIgnoreCase("evoluzioneTempo"))
				{
					mTitle = Utils.TranslateItalian(builder.toString());
					System.out.println("Title: " + mTitle);
					mState = eState.eFindContent;
				}
				break;
			case eContent:
				if (localName.equalsIgnoreCase("situazione")) 
				{
					String origContent = builder.toString();
					mContent = Utils.TranslateItalian(origContent);					
					mState = eState.eFindContent2;
				}
				break;
			case eContent2:
				if (localName.equalsIgnoreCase("puntipericolosi")) 
				{
					String origContent = builder.toString();
					mContent = mContent + "<br><br><strong>Danger Areas</strong><br>" + Utils.TranslateItalian(origContent);
					mContent = mContent + "<br>(machine-translated text)";

					System.out.print("Content:" + mContent + "\n");
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
			super.startElement(uri, localName, name, attributes);
			switch (mState)
			{
				case eFindDate: 
					if (localName.equalsIgnoreCase("giornoEmissione"))  
					{
						builder.setLength(0);
						mState = eState.eDate;
					}
					break;
				case eFindTitle: 
					if (localName.equalsIgnoreCase("evoluzioneTempo"))
					{
						builder.setLength(0);
						mState = eState.eTitle;
					}
					break;
				case eFindContent: 
					if (localName.equalsIgnoreCase("situazione"))  
					{
						builder.setLength(0);
						mState = eState.eContent;
					}		
					break;
				case eFindContent2: 
					if (localName.equalsIgnoreCase("puntipericolosi"))  
					{
						builder.setLength(0);
						mState = eState.eContent2;
					}		
					break;							
			}
	}
}
