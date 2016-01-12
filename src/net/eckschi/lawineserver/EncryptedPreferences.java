package src.net.eckschi.lawineserver;

//$Id$

import java.security.*;
import java.util.prefs.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class EncryptedPreferences extends ObfuscatedPreferences
{
private EncryptionStuff stuff;

protected EncryptedPreferences( AbstractPreferences parent, String name,
   AbstractPreferences target ) {
 super( parent, name, target );
}

private void setStuff( EncryptionStuff stuff ) {
 this.stuff = stuff;
}

private EncryptionStuff getStuff() {
 return stuff;
}

public String obfuscateString( String string ) {
 try {
   return getStuff().obfuscateString( string );
 } catch( GeneralSecurityException gse ) {
   gse.printStackTrace();
 }
 return null;
}

public String deObfuscateString( String string ) {
 try {
   return getStuff().deObfuscateString( string );
 } catch( GeneralSecurityException gse ) {
   gse.printStackTrace();
 }
 return null;
}

public WrappedPreferences wrapChild( WrappedPreferences parent,
                                           String name,
                                           AbstractPreferences child ) {
 EncryptedPreferences ep = new EncryptedPreferences( parent, name, child );
 ep.setStuff( stuff );
 return ep;
}

static public Preferences userNodeForPackage( Class clasz,
                                             SecretKey secretKey ) {
 AbstractPreferences ap =
   (AbstractPreferences)Preferences.userNodeForPackage( clasz );
 EncryptedPreferences ep = new EncryptedPreferences( null, "", ap );
 try {
   ep.setStuff( new EncryptionStuff( secretKey ) );
   return ep;
 } catch( GeneralSecurityException gse ) {
   gse.printStackTrace();
 }
 return null;
}

static public Preferences systemNodeForPackage( Class clasz,
                                             SecretKey secretKey ) {
 AbstractPreferences ap =
   (AbstractPreferences)Preferences.systemNodeForPackage( clasz );
 EncryptedPreferences ep = new EncryptedPreferences( null, "", ap );
 try {
   ep.setStuff( new EncryptionStuff( secretKey ) );
   return ep;
 } catch( GeneralSecurityException gse ) {
   gse.printStackTrace();
 }
 return null;
}
}
