package src.net.eckschi.lawineserver;

import java.util.prefs.*;

public class DelegatedPreferences extends AbstractPreferences
{
  private AbstractPreferences target;
  static private final boolean verbose = false;
 
  protected DelegatedPreferences( AbstractPreferences parent, String name,
      AbstractPreferences target ) {
    super( parent, name );
    this.target = target;
  }
 
  protected String getSpi( String key ) {
    if (verbose) {
      System.out.println( "DP["+target+"]:getSpi( "+key+" )" );
    }
 
    return target.get( key, null );
  }
 
  protected void putSpi( String key, String value ) {
    if (verbose) {
      System.out.println( "DP["+target+"]:putSpi( "+key+", "+value+" )" );
    }
 
    target.put( key, value );
  }
 
  protected void removeSpi( String key ) {
    if (verbose) {
      System.out.println( "DP["+target+"]:removeSpi( "+key+" )" );
    }
 
    target.remove( key );
  }
 
  protected AbstractPreferences childSpi( String name ) {
    if (verbose) {
      System.out.println( "DP["+target+"]:chlidSpi( "+name+" )" );
    }
 
    return (AbstractPreferences)target.node( name );
  }
 
  protected void removeNodeSpi() throws BackingStoreException {
    if (verbose) {
      System.out.println( "DP["+target+"]:removeNode()" );
    }
 
    target.removeNode();
  }
 
  protected String[] keysSpi() throws BackingStoreException {
    if (verbose) {
      System.out.println( "DP["+target+"]:keysSpi()" );
    }
 
    return target.keys();
  }
 
  protected String[] childrenNamesSpi() throws BackingStoreException {
    if (verbose) {
      System.out.println( "DP["+target+"]:childrenNamesSpi()" );
    }
 
    return target.childrenNames();
  }
 
  protected void syncSpi() throws BackingStoreException {
    if (verbose) {
      System.out.println( "DP["+target+"]:sync()" );
    }
 
    target.sync();
  }
 
 
  protected void flushSpi() throws BackingStoreException {
    if (verbose) {
      System.out.println( "DP["+target+"]:flush()" );
    }
 
    target.flush();
  }
}
