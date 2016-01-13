package src.net.eckschi.lawineserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.prefs.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Settings 
{
	private String mDataDir; 
    private String mServer;
    private String mUsername;
    private String mPassword;
    private String mServerDir;
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

	static private final String algorithm = "DES";
	// Read a file into a byte array
	static public byte[] readFile( String filename ) throws IOException {
		File file = new File( filename );
		long len = file.length();
		byte data[] = new byte[(int)len];
		FileInputStream fin = new FileInputStream( file );
		int r = fin.read( data );
		fin.close();
		if (r != len)
			throw new IOException( "Only read "+r+" of "+len+" for "+file );
		return data;
	}

	// Write byte array to a file
	static public void writeFile( String filename, byte data[] )
			throws IOException {
		FileOutputStream fout = new FileOutputStream( filename );
		fout.write( data );
		fout.close();
	}

	  
	private Settings()
	{
		try
		{
			byte rawKey[] = {(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xAF, (byte)0xBA, (byte)0xAD, (byte)0xF0, (byte)0x0D};//readFile( "key" );
			DESKeySpec dks = new DESKeySpec( rawKey );
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance( algorithm );
			SecretKey secretKey = keyFactory.generateSecret( dks );
	
			Preferences root = EncryptedPreferences.userNodeForPackage(LawineServer.class, secretKey );
	
			
			mDataDir = root.get("datadir", "/Users/manfredeckschlager/lawine/");
			mServer = root.get("server","");
		    mUsername = root.get("user", "");
		    mPassword = root.get("pass", "");
		    mServerDir = root.get("serverdir", "/eckschi/lawine/");

//			root.exportSubtree( System.out );
		}
		catch (Exception e)
		{
			
		}
	}
	
	public void NewValues()
	{
		try
		{
			byte rawKey[] = {(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xAF, (byte)0xBA, (byte)0xAD, (byte)0xF0, (byte)0x0D};//readFile( "key" );
			DESKeySpec dks = new DESKeySpec( rawKey );
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance( algorithm );
			SecretKey secretKey = keyFactory.generateSecret( dks );
	
			Preferences root = EncryptedPreferences.userNodeForPackage(LawineServer.class, secretKey );
				
			// local datadir
			System.out.println("local Datadir ["+ mDataDir + "]");
			Scanner scan = new Scanner(System.in);
			String temp = scan.nextLine(); 
			if (!temp.isEmpty())
				mDataDir = temp;
			// server
			System.out.println("Server ["+ mServer + "]");
			temp = scan.nextLine(); 
			if (!temp.isEmpty())
				mServer = temp;
	
			// user name
			System.out.println("Usernanme ["+ mUsername + "]");
			temp = scan.nextLine(); 
			if (!temp.isEmpty())
				mUsername = temp;
	
			// user name
			System.out.println("Password ["+ mPassword + "]");
			temp = scan.nextLine(); 
			if (!temp.isEmpty())
				mPassword = temp;
	
			// server dir
			System.out.println("Password ["+ mServerDir + "]");
			temp = scan.nextLine(); 
			if (!temp.isEmpty())
				mServerDir = temp;
			
			scan.close();
		   
			// store values
		    root.put("datadir", mDataDir);
		    root.put("server", mServer);
		    root.put("user", mUsername);
		    root.put("pass", mPassword);
		    root.put("serverdir", mServerDir);
		}
		catch (Exception e)
		{
			
		}
	}
	
	private static Settings instance = null;
}