package org.restfest.vimeo

import java.net.URI

import argonaut._, Argonaut._


case class Image(href: URI, width: Int, height: Int)

object Image {
  implicit val imageDecoder: DecodeJson[Image] = DecodeJson{ c =>
    for {
      href <- (c --\ "link").as[URI]
      width <- (c --\ "width").as[Int]
      height <- (c --\ "height").as[Int]
    } yield {
      Image(
        width = width,
        height = height,
        href = href
      )
    }
  }

  implicit val imageEncoder: EncodeJson[Image] = EncodeJson{ img =>
    Json.obj(
      "href" := img.href,
      "width" := img.width,
      "height" := img.height
    )
  }
}

case class Metadata(duration: Int, width: Int, height: Int)

object Metadata{
  implicit val metadataEncoder: EncodeJson[Metadata] = EncodeJson{ m =>
    Json.obj(
      "duration" := m.duration,
      "width"    := m.width,
      "height"   := m.height
    )
  }
}

case class VimeoVideo(title: String, description:String, href: URI, embed: String, images: List[Image], metadata: Metadata) {
  def tupled = title.split("\\\\") match {
    case Array(year, speaker, kind) => (year.trim, speaker.trim, kind.trim)
    case Array(year, speaker) => (year.trim, speaker.trim, "feature")
    case a => (a.mkString("\\"), "unknown", "unknown")
  }
}

object VimeoVideo {
  implicit val VVDecoder = DecodeJson{ c =>
    for {
      name <- (c --\ "name").as[String]
      description <- (c --\ "description").as[String]
      href <- (c --\ "link").as[URI]
      embed <- (c --\ "embed" --\ "html").as[String]
      duration <- (c --\ "duration").as[Int]
      width <- (c --\ "width").as[Int]
      height <- (c --\ "height").as[Int]
      pictures <- (c --\ "pictures" --\ "sizes").as[List[Image]]
      //comments <- (c --\ "metadata" --\ "connections" --\ "comments" --\ "uri").as[String]
    } yield {
      val meta = Metadata(duration, width, height)
      VimeoVideo(
        title = name,
        description = description,
        href = href,
        embed = embed,
        images = pictures,
        metadata = meta
      )
    }
  }
}
