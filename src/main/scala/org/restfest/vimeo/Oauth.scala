package org.restfest.vimeo

import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64

import okhttp3.{FormBody, HttpUrl, Request}

object Oauth {
  case class ClientInfo(authURI: URI, id: ClientId, secret: ClientSecret) {
    def clientCredentialsRequest: Request = {
      new Request.Builder().
        url(HttpUrl.get(authURI)).
        post(new FormBody.Builder().add("grant_type", "client_credentials").add("scope", "public").build()).
        addHeader("Authorization", "Basic " + Base64.getEncoder.encodeToString(s"${id.asString}:${secret.asString}".getBytes(StandardCharsets.UTF_8))).
        build()
    }
  }


  case class ClientSecret(asString: String) extends AnyVal
  case class ClientId(asString: String) extends AnyVal

  case class AccessToken(asString: String) extends AnyVal
  case class RefreshToken(asString: String) extends AnyVal

  sealed trait TokenType {
    def name: String
  }

  object TokenType {
    def apply(name: String): TokenType = name match {
      case Bearer.name => Bearer
      case x => sys.error(x + " is not supported")
    }

    case object Bearer extends TokenType { val name = "bearer" }
  }

  case class AccessTokenResponse(token: AccessToken, tokenType: TokenType)

  object AccessTokenResponse {
    import argonaut._, Argonaut._

    implicit val AccessTokenCodec = CodecJson.derived[String].xmap(AccessToken)(_.asString)
    implicit val TokenTypeCodec = CodecJson.derived[String].xmap(TokenType.apply)(_.name)

    implicit val ATRCodec = casecodec2(AccessTokenResponse.apply, AccessTokenResponse.unapply)("access_token", "token_type")
  }
}
