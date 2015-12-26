package src.net.eckschi.lawineserver;
import java.net.HttpURLConnection;
import java.io.*;
import java.net.Proxy;
import java.net.URL;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


public class AvalancheReport extends DefaultHandler implements LexicalHandler
{
	protected String mDate;
	protected String mAuthor;
	protected String mContent;
	protected String mTitle;
	protected String mBitmapLocation;
	protected String mImgOnDevice;
	protected String mUrlStr;
	protected String mEncoding;
	protected String mCopyright;

	protected StringBuilder builder;

	public String GetDate() { return mDate; }
	public String GetAuthor() { return mAuthor; }
	public String GetContent() { return mContent; }
	public String GetTitle() { return mTitle; }
	public String GetImgOnDevice() { return mImgOnDevice; }
	public String GetCopyright() {return mCopyright;}

	public AvalancheReport()
	{	
		mCopyright = "© ";
		mAuthor ="";
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] 
		{
				new X509TrustManager() 
				{
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
				}
		};

		// Install the all-trusting trust manager
		try 
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (Exception e)
		{
			
		}
		 // Create all-trusting host name verifier
		 HostnameVerifier allHostsValid = new HostnameVerifier() {
		     public boolean verify(String hostname, SSLSession session) { return true; }
		 };
		 // Install the all-trusting host verifier
		 HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

    public void comment(char[] ch, int start, int length) throws SAXException 
    {
    	//Log.i("asdf", "<!-- comment: " + String.valueOf(ch, start, length) + " -->");
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException { }
    public void endDTD() throws SAXException { }
    public void startEntity(String name) throws SAXException { }
    public void endEntity(String name) throws SAXException { }
    public void startCDATA() throws SAXException { }
    public void endCDATA() throws SAXException { } 

	public boolean parse()
	{
			try
			{
				// SAX
				//SAXParserFactory factory = SAXParserFactory.newInstance();
				//SAXParser parser = factory.newSAXParser();
				//XMLReader xr = parser.getXMLReader();		
			
				// tagsoup
				XMLReader xr = new Parser(); 
				xr.setContentHandler(this);
				xr.setErrorHandler(this);
				xr.setProperty("http://xml.org/sax/properties/lexical-handler", this);
				System.out.println(mUrlStr);
				//URL url = new URL("http", "www.lawine.salzburg.at", 80, "/lagebericht.asp");
				URL url = new URL(mUrlStr);
				long startTime = System.currentTimeMillis();
                HttpURLConnection c = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
		        c.setRequestMethod("GET");
		        c.setDoOutput(true);
		        c.setReadTimeout(10000);
				c.connect();
				InputStream ist = c.getInputStream();
                System.out.println("finished in "
                        + ((System.currentTimeMillis() - startTime) / 1000)
                        + " sec");
				InputSource is = new InputSource(ist);

				is.setEncoding(mEncoding);
				xr.parse(is);
				return true;
			}
			catch (Exception e)
			{
				System.out.print(e.toString());
				return false;
			}	
	}	
}
