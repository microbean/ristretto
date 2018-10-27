module javax.enterprise.inject.spi {
  exports javax.enterprise.inject.spi;
  exports javax.enterprise.inject.spi.configurator;

  requires transitive javax.enterprise.context.spi;
  requires transitive javax.enterprise.event;
  requires transitive javax.enterprise.inject;
  requires transitive javax.interceptor;

  requires transitive static javax.el;
}
