package cellar

import coursierapi.{Dependency, Module}

final case class MavenCoordinate(group: String, artifact: String, version: String):
  def render: String = s"$group:$artifact:$version"

  def toCoursierDependency: Dependency =
    Dependency.of(Module.of(group, artifact, java.util.Collections.emptyMap()), version)

object MavenCoordinate:
  def parse(raw: String): Either[String, MavenCoordinate] =
    raw.split(":", -1).toList match
      case g :: a :: v :: Nil if g.trim.nonEmpty && a.trim.nonEmpty && v.trim.nonEmpty =>
        Right(MavenCoordinate(g, a, v))
      case _ =>
        Left(
          s"Invalid coordinate '$raw'. Expected format: group:artifact:version (exactly three non-empty segments separated by ':')"
        )
