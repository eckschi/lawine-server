package src.net.eckschi.lawineserver;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;


public class FetchSwitzerland extends AvalancheReport 
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
		eCopyright
	};
	
	private eState mState = eState.eFindDate; 
	private String mImageURL1;
	private String mImageURL2;
	private String mTempImage1;
	private String mTempImage2;
	
	//-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public FetchSwitzerland(ReportWriter rw)
	{
		super();

		// search for the id of the report in the main page
		mUrlStr = "http://www.slf.ch/lawineninfo/lawinenbulletin/nationale_lawinenbulletins/index_DE";
        mImageURL1 = "http://www.slf.ch/avalanche/img/gkhg_de_c.gif";
        mImageURL2 = "http://www.slf.ch/avalanche/img/hn1_de_c.gif";	

        // then load and parse page with that particular id
		mEncoding = "UTF-8";
		mCopyright = "© 1998-2012, WSL-Institut für Schnee- und Lawinenforschung SLF";
		mAuthor = "";
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
				if (localName.equalsIgnoreCase("p"))
				{
					mDate = builder.toString();
					System.out.println("Date: " + mDate);
					mState = eState.eFindTitle;
				}
				break;
			case eTitle: 
				if (localName.equalsIgnoreCase("p"))
				{
					mTitle = builder.toString();
					System.out.println("Title: " + mTitle);
					mState = eState.eFindContent;
				}
				break;
			case eContent:
			    if (localName.equalsIgnoreCase("h3"))
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
				case eFindDate: 
					if (localName.equalsIgnoreCase("p"))  
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("valid")) 
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
						if ((attr != null) && attr.equals("flash")) 
						{	
							builder.setLength(0);
							mState = eState.eTitle;
						}
					}
					break;
				case eFindContent: 
					if (localName.equalsIgnoreCase("p"))  
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("current-condition")) 
						{	
							builder.setLength(0);
							mState = eState.eContent;
						}
					}		
					break;
				case eContent:
					if (localName.equalsIgnoreCase("p"))  
					{
						String attr = attributes.getValue("class");
						if ((attr != null) && attr.equals("copy-right")) 
						{	
							mContent =  builder.toString();					
							System.out.println("Content: " + mContent);
							mState = eState.eCopyright;
						}
					}
					else if (localName.equalsIgnoreCase("h3"))
					{
						builder.append("<br><strong>");
					}
					break;
			}
	}
}
