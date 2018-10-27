module org.microbean.ristretto.javax.enterprise.inject {
  exports javax.enterprise.inject;

  requires transitive org.microbean.ristretto.javax.enterprise.context; // just for RequestScoped.class :-O
  requires transitive org.microbean.ristretto.javax.enterprise.util;
  requires transitive org.microbean.ristretto.javax.inject;
}
