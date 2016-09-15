package org.restfest.vimeo

import java.io.{File, FileInputStream, IOException}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import Oauth._
import okhttp3._

import scala.concurrent._
import argonaut._
import Argonaut._

import scalaz.\/


object Main extends App {
  val authURI = URI.create("https://api.vimeo.com/oauth/authorize/client")

  if (args.length < 1) {
    println("""
      |Usage:
      |
      |Main <channelId>
    """.stripMargin)
    sys.exit(1)
  }


  val clientInfo = loadClientInfo()
  val channelId = args(0)

  val client = new OkHttpClient.Builder().build()

  def loadClientInfo(): ClientInfo = {
    val file = new File("secrets.properties")
    if (!file.exists()) {
      println("We need a secrets.properties in the current working directory")
      sys.exit(1)
    }
    import scala.collection.JavaConverters._
    val props = new java.util.Properties()
    props.load(new FileInputStream(file))
    val map = props.asScala

    ClientInfo(authURI, ClientId(map("id")), ClientSecret(map("secret")))
  }


  def requestAsSync[T: DecodeJson](req: VimeoRequest): String \/ VimeoResponse[T] = {
    requestAsSync(req.toGetRequest())
  }

  def requestAsSync[T: DecodeJson](req: Request): String \/ VimeoResponse[T] = {
    for {
      response <- \/.fromTryCatchNonFatal(client.newCall(req).execute()).leftMap(_.getMessage)
      decoded <- response.body().string().decodeEither[T]
    } yield {
      println(response)
      VimeoResponse[T](response.headers(), decoded)
    }
  }

  def getChannel(token: AccessToken, channelId: String): Vector[VimeoVideo] = {
    def meh(vimeoRequest: VimeoRequest, vb: Vector[VimeoVideo]): Vector[VimeoVideo] = {
      (for {
        page <- requestAsSync[ChannelResponse](vimeoRequest)
      } yield {
        vb ++ page.data.next.map(uri => meh(VimeoRequest(token, uri), page.data.data)).getOrElse(page.data.data)
      }).getOrElse(vb)
    }
    meh(ChannelRequest(channelId).toRequest(token), Vector.empty)
  }


  val exportDir = new File("target/export")
  if (!exportDir.exists() && !exportDir.mkdirs()) {
    sys.error("Unable to create export directory")
  }

  val flow = for {
    response <- requestAsSync[AccessTokenResponse](clientInfo.clientCredentialsRequest)
    channel <- \/.right(getChannel(response.data.token, channelId))
  } yield {
    channel
  }

  val json = flow.map{ res =>
    Json.obj("videos" := res.map{v =>
      val (year, speaker, kind) = v.tupled

      Json.obj(
        "year"        := year,
        "speaker"     := speaker,
        "kind"        := kind,
        "description" := v.description,
        "embed"       := v.embed,
        "href"        := v.href,
        "images"      := v.images,
        "metadata"    := v.metadata
      )
    })
  }

  json.foreach(j => Files.write(new File(s"target/export/$channelId.json").toPath, j.spaces2.getBytes(StandardCharsets.UTF_8)))

  client.dispatcher().executorService().shutdown()
}
