module org.microbean.ristretto.javax.enterprise.inject.spi {
  exports javax.enterprise.inject.spi;
  exports javax.enterprise.inject.spi.configurator;

  requires transitive org.microbean.ristretto.javax.enterprise.context.spi;
  requires transitive org.microbean.ristretto.javax.enterprise.event;
  requires transitive org.microbean.ristretto.javax.enterprise.inject;
  requires transitive org.microbean.ristretto.javax.interceptor;

  requires transitive static org.microbean.ristretto.javax.el;
}
