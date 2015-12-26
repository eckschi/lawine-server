package src.net.eckschi.lawineserver;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.sun.xml.ws.util.ByteArrayBuffer;

public class Utils 
{
	
    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
    static public void DownloadFromUrl(String imageURL, String fileName/*, boolean dontOverwirte*/) 
    {  
    	//this is the downloader method
            try 
            {
                    URL url = new URL(imageURL); //you can write here any link
                    //File file = new File(fileName);
                    File file = new File(/*LawineActivity.DataDir*,*/ fileName);
                    long startTime = System.currentTimeMillis();
                    System.out.println("ImageManager download url:" + url);
                    System.out.println("ImageManager download file name:" + fileName);
                    /* Open a connection to that URL. */
                    URLConnection ucon = url.openConnection(Proxy.NO_PROXY);
                    InputStream is = ucon.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ByteArrayBuffer baf = new ByteArrayBuffer(1500);
                    int current = 0;
                    while ((current = bis.read()) != -1) 
                    {
                            baf.write((byte) current); //append
                    }
                    /* Convert the Bytes read to a String. */
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(baf.toByteArray());
                    baf.close();
                    fos.close();
                    System.out.println("ImageManager download finished in "
                                    + ((System.currentTimeMillis() - startTime) / 1000)
                                    + " sec");
            } 
            catch (IOException e) 
            {
            	System.out.println("ImageDownload exception: " + e + " while downloading " + imageURL);
            }
     }
    
    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
    public static Image makeColorTransparent (Image im, final Color color) 
	{
    	ImageFilter filter = new RGBImageFilter() 
		{
    		// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;
			
			public final int filterRGB(int x, int y, int rgb) 
			{
				if ( ( rgb | 0xFF000000 ) == markerRGB ) 
				{
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				}
				else 
				{
					// nothing to do
					return rgb;
				}
			}
		}; 
		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	 }

    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	static public String TranslateItalian(String Input)
	{
		String Output = "";
		try
		{
			Output = Translate.execute(Input, Language.ITALIAN, Language.ENGLISH);//Language.GERMAN);
			Output = Output.replace("pond farms", "areas with drift snow");
			Output = Output.replace("low overhead", "little extra weight");
			Output = Output.replace("blanket", "layer");
			Output = Output.replace("mantle", "snow cover");
			Output = Output.replace("sottocresta", "below ridges");
			Output = Output.replace("pond", "accumulations");
			}
		catch (Exception e)
		{
			System.out.println("lost in translation: " + e.toString());
		}
		return Output;
	}

    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	static public String TranslateEnglish(String Input)
	{
		String Output = "";
		try
		{
			Output = Translate.execute(Input, Language.GERMAN, Language.ENGLISH);//Language.GERMAN);
			/*Output = Output.replace("pond farms", "areas with drift snow");
			Output = Output.replace("low overhead", "little extra weight");
			Output = Output.replace("blanket", "layer");
			Output = Output.replace("mantle", "snow cover");
			Output = Output.replace("sottocresta", "below ridges");
			Output = Output.replace("pond", "accumulations"); */
			}
		catch (Exception e)
		{
			System.out.println("lost in translation: " + e.toString());
		}
		return Output;
	}

    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
	public static void filecopy(String fromFileName, String toFileName) throws IOException 
	{
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists()) throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
		if (!fromFile.isFile()) throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
		if (!fromFile.canRead()) throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

		if (toFile.isDirectory())
		  	toFile = new File(toFile, fromFile.getName());

		String parent = toFile.getParent();
		if (parent == null)
			parent = System.getProperty("user.dir");
		File dir = new File(parent);
		if (!dir.exists()) throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
		if (dir.isFile()) throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
		if (!dir.canWrite()) throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);

	    FileInputStream from = null;
	    FileOutputStream to = null;
	    try 
	    {
	    	from = new FileInputStream(fromFile);
	    	to = new FileOutputStream(toFile);
	    	byte[] buffer = new byte[4096];
	    	int bytesRead;

		    while ((bytesRead = from.read(buffer)) != -1)
		        to.write(buffer, 0, bytesRead); // write
		} 
	    finally 
	    {
	    	if (from != null)
	    		try 
	    		{
	    			from.close();
		        } 
	    		catch (IOException e) 
	    		{
		          ;
		        }
		      	if (to != null)
		        try 
		      	{
		        	to.close();
		        } catch (IOException e) {
		          ;
		        }
	    }
	}
}

