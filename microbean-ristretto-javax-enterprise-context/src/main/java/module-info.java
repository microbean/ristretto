module org.microbean.ristretto.javax.enterprise.context {
  exports javax.enterprise.context;

  requires transitive org.microbean.ristretto.javax.enterprise.util;
  requires transitive org.microbean.ristretto.javax.inject;
}
