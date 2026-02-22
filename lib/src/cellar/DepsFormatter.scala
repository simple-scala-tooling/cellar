package cellar

object DepsFormatter:
  def format(resolved: ResolvedDeps): String =
    val sb = new StringBuilder
    sb.append(s"${resolved.root.render}\n")
    sb.append(s"  (${resolved.deps.size} transitive dependencies)\n\n")
    resolved.deps.foreach { dep =>
      val g = dep.getModule.getOrganization
      val a = dep.getModule.getName
      val v = dep.getVersion
      sb.append(s"  $g:$a:$v\n")
    }
    sb.toString.stripTrailing()
