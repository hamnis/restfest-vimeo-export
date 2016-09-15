package org.restfest

import java.net.URI

import argonaut.CodecJson

package object vimeo {
  implicit val uriCodec: CodecJson[URI] = CodecJson.derived[String].xmap(URI.create)(_.toString)
}
