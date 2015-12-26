package src.net.eckschi.lawineserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public class FtpUploader 
{
	private FTPClient ftp;
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	public boolean Connect()
	{
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        try
        {
            int reply;
            ftp.connect(Settings.Instance().GetServer());
            System.out.println("Connected to " + Settings.Instance().GetServer() + ".");

            // After connection attempt, you should check the reply code to verify success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                return false;
            }
        }
        catch (IOException e)
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            return false;
        }
        
        // login
        try
        {
            if (!ftp.login(Settings.Instance().GetUsername(), Settings.Instance().GetPassword()))
            {
                ftp.logout();
                return false;
            }

            System.out.println("Remote system is " + ftp.getSystemName());

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            // Use passive mode as default because most of us are behind firewalls these days.
            ftp.enterLocalPassiveMode();
            return true;
        }
        catch (FTPConnectionClosedException ex)
        {
            System.err.println("Server closed connection.");
            ex.printStackTrace();
            return false;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
	}
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public boolean Upload(String filename, String dest)
    {
		boolean error = false;
		InputStream input;

        try
        {
            input = new FileInputStream(filename);
            ftp.storeFile(Settings.Instance().GetServerDir() + dest, input);
            input.close();
        }
        catch (FTPConnectionClosedException e)
        {
        	System.err.println("Server closed connection.");
        	e.printStackTrace();
        	error = true;
        }
        catch (Exception e)
        {
        	error = true;
        	System.err.println(e.toString());
        }
        return !error;
    }
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------

	public void CreateDirectoryStructure(String dirs[])
	{
		for (int i=0; i<dirs.length; i++)
		{
			try
			{
				ftp.makeDirectory(Settings.Instance().GetServerDir()+dirs[i]);
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
		}
		
	}
	
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
	public void Disconnect()
	{
		try
		{
			ftp.logout();
		}
		catch (IOException f)
		{
			// do nothing
			System.err.println(f.toString());
		}
		
    	if (ftp.isConnected())
    	{
    		try
    		{
    			ftp.disconnect();
    		}
    		catch (IOException f)
    		{
    			// do nothing
    			System.err.println(f.toString());
    		}
    	}
	}
}
