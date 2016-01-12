package src.net.eckschi.lawineserver;

import java.util.prefs.*;


abstract public class WrappedPreferences extends DelegatedPreferences
{
  protected WrappedPreferences( AbstractPreferences parent, String name,
      AbstractPreferences target ) {
    super( parent, name, target );
  }
 
  protected AbstractPreferences childSpi( String name ) {
    return wrapChild( this, name,
                      (AbstractPreferences)super.childSpi( name ) );
  }
 
  public WrappedPreferences wrapChild( WrappedPreferences parent,
                                              String name,
                                              AbstractPreferences child ) {
    throw new UnsupportedOperationException(
      "You must override WrappedPreferences.wrapChild()" );
  }
}

