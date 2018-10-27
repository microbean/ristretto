module org.microbean.ristretto.javax.decorator {
  exports javax.decorator;

  requires transitive org.microbean.ristretto.javax.enterprise.inject; // for Stereotype.class
}
