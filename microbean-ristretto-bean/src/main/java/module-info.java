module org.microbean.ristretto.bean {
  
  requires org.microbean.ristretto.javax.enterprise.context;
  requires org.microbean.ristretto.javax.enterprise.inject.spi;
  requires org.microbean.ristretto.javax.enterprise.util;

  requires transitive org.microbean.development.annotation;
  requires transitive org.microbean.ristretto.context;
  
  exports org.microbean.ristretto.bean;
  exports org.microbean.ristretto.type;
  
}
