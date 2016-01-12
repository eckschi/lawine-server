package src.net.eckschi.lawineserver;

import java.util.prefs.*;

public abstract class ObfuscatedPreferences extends WrappedPreferences
{
  protected ObfuscatedPreferences( AbstractPreferences parent, String name,
      AbstractPreferences target ) {
    super( parent, name, target );
  }
 
  protected String getSpi( String key ) {
    return deObfuscateString( super.getSpi( obfuscateString( key ) ) );
  }
 
  protected void putSpi( String key, String value ) {
    super.putSpi( obfuscateString( key ), obfuscateString( value ) );
  }
 
  protected void removeSpi( String key ) {
    super.removeSpi( obfuscateString( key ) );
  }
 
  protected String[] keysSpi() throws BackingStoreException {
    String keys[] = super.keysSpi();
    String dkeys[] = (String[])keys.clone();
    for (int i=0; i<dkeys.length; ++i) {
      dkeys[i] = deObfuscateString( dkeys[i] );
    }
    return dkeys;
  }
 
  abstract public String obfuscateString( String string );
  abstract public String deObfuscateString( String string );
}

