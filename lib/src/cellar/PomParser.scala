package cellar

import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilderFactory

final case class PomMeta(
    coordinate: MavenCoordinate,
    name: Option[String],
    description: Option[String],
    url: Option[String],
    licenses: Seq[(String, Option[String])],
    scm: Option[String],
    developers: Seq[(String, Option[String])],
)

object PomParser:
  def parse(path: fs2.io.file.Path, coord: MavenCoordinate): PomMeta =
    val doc  = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toNioPath.toFile)
    val root = doc.getDocumentElement

    def text(tag: String): Option[String] =
      val kids = root.getChildNodes
      (0 until kids.getLength)
        .map(kids.item)
        .collectFirst { case e: Element if e.getTagName == tag => e.getTextContent.trim }
        .filter(_.nonEmpty)

    def children(parentTag: String, childTag: String): Seq[Element] =
      val parents = root.getElementsByTagName(parentTag)
      if parents.getLength == 0 then Seq.empty
      else
        val parent = parents.item(0)
        val kids   = parent.getChildNodes
        (0 until kids.getLength)
          .map(kids.item)
          .collect { case e: Element if e.getTagName == childTag => e }

    def elemText(el: Element, tag: String): Option[String] =
      val nodes = el.getElementsByTagName(tag)
      Option.when(nodes.getLength > 0)(nodes.item(0).getTextContent.trim).filter(_.nonEmpty)

    val licenses = children("licenses", "license").map { el =>
      (elemText(el, "name").getOrElse(""), elemText(el, "url"))
    }

    val developers = children("developers", "developer").map { el =>
      (elemText(el, "name").getOrElse(""), elemText(el, "email"))
    }

    val scm =
      val scms = root.getElementsByTagName("scm")
      if scms.getLength == 0 then None
      else elemText(scms.item(0).asInstanceOf[Element], "url")

    PomMeta(
      coordinate = coord,
      name = text("name"),
      description = text("description"),
      url = text("url"),
      licenses = licenses,
      scm = scm,
      developers = developers,
    )
