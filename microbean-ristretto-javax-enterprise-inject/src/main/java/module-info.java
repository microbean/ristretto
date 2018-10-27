module javax.enterprise.inject {
  exports javax.enterprise.inject;

  requires transitive javax.enterprise.context; // just for RequestScoped.class :-O
  requires transitive javax.enterprise.util;
  requires transitive javax.inject;
}
