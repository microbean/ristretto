module org.microbean.ristretto.bean {

  requires transitive org.microbean.ristretto.context;
  requires transitive org.microbean.ristretto.javax.enterprise.context;
  requires transitive org.microbean.ristretto.javax.enterprise.inject.spi;
  requires transitive org.microbean.ristretto.javax.enterprise.util;
  
  exports org.microbean.ristretto.bean;
  
}
