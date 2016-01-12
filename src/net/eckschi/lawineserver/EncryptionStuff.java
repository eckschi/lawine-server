package src.net.eckschi.lawineserver;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
 
public class EncryptionStuff
{
  static private final String algorithm = "DES";
  static private SecureRandom sr = new SecureRandom();
  private SecretKey secretKey;
  private Cipher cipher;
 
  public EncryptionStuff( SecretKey secretKey ) throws GeneralSecurityException {
    this.secretKey = secretKey;
 
    cipher = Cipher.getInstance( algorithm );
  }
 
  public String arrayToString( byte raw[] ) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<raw.length; ++i) {
      short s = (short)raw[i];
      if (s>0)
        s += 256;
      int hi = s>>4;
      int lo = s&0xf;
      sb.append( (char)('a'+hi) );
      sb.append( (char)('a'+lo) );
    }
    return sb.toString();
  }
 
  public byte[] stringToArray( String string ) {
    StringBuffer sb = new StringBuffer( string );
    int len = sb.length();
 
    if ((len&1)==1)
      throw new RuntimeException( "String must be of even length! "+string );
 
    byte raw[] = new byte[len/2];
    int ii=0;
    for (int i=0; i<len; i+=2) {
      int hic = sb.charAt( i ) - 'a';
      int loc = sb.charAt( i+1 ) - 'a';
      byte b = (byte)( (hic<<4) | loc );
      raw[ii++] = b;
    }
    return raw;
  }
 
  synchronized public String obfuscateString( String string )
      throws GeneralSecurityException {
    cipher.init( Cipher.ENCRYPT_MODE, secretKey, sr );
    byte raw[] = string.getBytes();
    byte oraw[] = cipher.doFinal( raw );
    String ostring = arrayToString( oraw );
    return ostring;
  }
 
  synchronized public String deObfuscateString( String string )
      throws GeneralSecurityException {
    cipher.init( Cipher.DECRYPT_MODE, secretKey, sr );
    byte raw[] = stringToArray( string );
    byte draw[] = cipher.doFinal( raw );
    String dstring = new String( draw );
    return dstring;
  }
}