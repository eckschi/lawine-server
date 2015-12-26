package src.net.eckschi.lawineserver;

public class Settings 
{
	private static final String mDataDir = "/Users/manfredeckschlager/lawine/";
//	private static final String mDataDir = "/home/meckschlager/lawine/";
    private static final String mServer = "s316512177.online.de";
    private static final String mUsername = "";
    private static final String mPassword = "";
    private static final String mServerDir = "/eckschi/lawine/";
	public String GetDataDir() { return mDataDir; }
	public String GetServer() { return mServer; }
	public String GetUsername() { return mUsername; }
	public String GetPassword() { return mPassword; }
	public String GetServerDir() { return mServerDir; }
	public static Settings Instance()
	{
		if (instance == null)
			instance = new Settings();
		return instance;
	}
	
	private Settings()
	{
		
	}
	
	private static Settings instance = null;
}