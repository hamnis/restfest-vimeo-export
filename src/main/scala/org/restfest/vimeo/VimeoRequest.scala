package org.restfest.vimeo

import java.net.URI

import org.restfest.vimeo.Oauth.AccessToken
import okhttp3.{Headers, HttpUrl, Request}

case class VimeoRequest(token: AccessToken, uri: URI) {
  def toGetRequest(): Request = {
    new Request.Builder()
      .url(HttpUrl.get(uri))
      .addHeader("Authorization", s"Bearer ${token.asString}")
      .get()
      .build()
  }
}
case class VimeoResponse[T](headers: Headers, data: T)

case class ChannelRequest(channelId: String) {
  def toRequest(token: AccessToken) = VimeoRequest(token, URI.create(s"https://api.vimeo.com/channels/${channelId}/videos"))
}

case class ChannelResponse(data: Vector[VimeoVideo], next: Option[URI])

object ChannelResponse {
  import argonaut._, Argonaut._

  implicit val channelResponseDecoder = DecodeJson{ c =>
    for {
      data <- (c --\ "data").as[Vector[VimeoVideo]]
      paging <- (c --\ "paging" --\ "next").as[Option[String]]
    } yield {
      ChannelResponse(data, paging.map(s => "https://api.vimeo.com" + s).map(URI.create))
    }
  }
}
