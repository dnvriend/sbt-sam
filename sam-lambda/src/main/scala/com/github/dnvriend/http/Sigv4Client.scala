package com.github.dnvriend.http

import java.net.URL
import java.time.{LocalDateTime, ZoneId}

import com.amazonaws.auth.internal.AWS4SignerUtils
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.{AwsRegionProvider, DefaultAwsRegionProviderChain}
import io.ticofab.AwsSigner

import scala.compat.Platform
import scalaj.http.{Http, HttpResponse}

object Sigv4Client {
  def getSignedHeaders(url: URL,
                       awsCredentialProvider: AWSCredentialsProvider,
                       region: String,
                       method: String,
                      ): Map[String, String] = {
    def clock(): LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    val dateIso8601: String = AWS4SignerUtils.formatTimestamp(Platform.currentTime)
    val hostHeader: Map[String, String] = Map("Host" -> url.getHost)
    val headers: Map[String, String] = Map("Date" -> dateIso8601) ++ hostHeader
    val service: String = "execute-api"
    val signer: AwsSigner = io.ticofab.AwsSigner(awsCredentialProvider, region, service, clock)
    signer.getSignedHeaders(
      url.getPath,
      method,
      Map.empty,
      headers,
      None
    )
  }

  /**
    * Connect to given url and executes a GET request with DefaultAWSCredentialsProviderChain and
    * AwsRegionProviderChain to lookup the API keys and REGION for a given AWS profile
    */
  def get(url: String,
          headers: Map[String, String],
         ): HttpResponse[Array[Byte]] = {
    get(new URL(url), new DefaultAWSCredentialsProviderChain, new DefaultAwsRegionProviderChain, headers)
  }

  /**
    * Connect to given url and executes a GET request
    */
  def get(url: URL,
           credentialsProvider: AWSCredentialsProvider,
            regionProvider: AwsRegionProvider,
           headers: Map[String, String],
         ): HttpResponse[Array[Byte]] = {
    val mergedHeaders: Map[String, String] = headers ++ getSignedHeaders(url, credentialsProvider, regionProvider.getRegion, "GET")
    Http(url.toString).headers(mergedHeaders).timeout(Int.MaxValue, Int.MaxValue).asBytes
  }


}
