package com.github.dnvriend.http

import java.net.URL
import java.time.{LocalDateTime, ZoneId}

import com.amazonaws.auth.internal.AWS4SignerUtils
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.{AwsRegionProvider, DefaultAwsRegionProviderChain}
import io.ticofab.AwsSigner
import play.api.libs.json.{Json, Writes}

import scala.compat.Platform
import scalaj.http.{Http, HttpResponse}

object Sigv4Client {
  def getSignedHeaders(url: URL,
                       awsCredentialProvider: AWSCredentialsProvider,
                       region: String,
                       method: String,
                       payload: Option[Array[Byte]] = None,
                       headersToEncrypt: Map[String, String] = Map.empty
                      ): Map[String, String] = {
    val clock: () => LocalDateTime = () => LocalDateTime.now(ZoneId.of("UTC"))
    val dateIso8601: String = AWS4SignerUtils.formatTimestamp(Platform.currentTime)
    val headers: Map[String, String] = {
      Map(
        "Host" -> url.getHost,
        "X-Amz-Date" -> dateIso8601,
      ) ++ headersToEncrypt
    }
    val service: String = "execute-api"
    val signer: AwsSigner = io.ticofab.AwsSigner(awsCredentialProvider, region, service, clock)
    signer.getSignedHeaders(
      url.getPath,
      method,
      Map.empty,
      headers,
      payload
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

  /**
    * Connect to given url and executes a POST request with DefaultAWSCredentialsProviderChain and
    * AwsRegionProviderChain to lookup the API keys and REGION for a given AWS profile
    */
  def post[A: Writes](
                       value: A,
                       url: String,
                       headers: Map[String, String]): HttpResponse[Array[Byte]] = {
    post(value, new URL(url), new DefaultAWSCredentialsProviderChain, new DefaultAwsRegionProviderChain, headers)
  }

  /**
    * Connect to given url and executes a POST request
    */
  def post[A: Writes](
                       value: A,
                       url: URL,
                       credentialsProvider: AWSCredentialsProvider,
                       regionProvider: AwsRegionProvider,
                       headers: Map[String, String],
                     ): HttpResponse[Array[Byte]] = {
    val payload: String = Json.toJson(value).toString()
    val payloadAsBytes: Array[Byte] = payload.getBytes("UTF-8")
    val mergedHeaders: Map[String, String] = {
      headers ++ getSignedHeaders(
        url,
        credentialsProvider,
        regionProvider.getRegion,
        "POST",
        Some(payloadAsBytes),
        Map("Content-Type" -> "application/json")
      )
    }
    println(mergedHeaders)
    Http(url.toString)
      .headers(mergedHeaders)
      .timeout(Int.MaxValue, Int.MaxValue)
      .compress(true)
      .postData(payload)
      .asBytes
  }

  /**
    * Connect to given url and executes a PUT request with DefaultAWSCredentialsProviderChain and
    * AwsRegionProviderChain to lookup the API keys and REGION for a given AWS profile
    */
  def put[A: Writes](
                      value: A,
                      url: String,
                      headers: Map[String, String]): HttpResponse[Array[Byte]] = {
    put(value, new URL(url), new DefaultAWSCredentialsProviderChain, new DefaultAwsRegionProviderChain, headers)
  }

  /**
    * Connect to given url and executes a PUT request
    */
  def put[A: Writes](
                      value: A,
                      url: URL,
                      credentialsProvider: AWSCredentialsProvider,
                      regionProvider: AwsRegionProvider,
                      headers: Map[String, String],
                    ): HttpResponse[Array[Byte]] = {
    val payload: String = Json.toJson(value).toString()
    val payloadAsBytes: Array[Byte] = payload.getBytes("UTF-8")
    val mergedHeaders: Map[String, String] = {
      headers ++ getSignedHeaders(
        url,
        credentialsProvider,
        regionProvider.getRegion,
        "PUT",
        Some(payloadAsBytes),
        Map("Content-Type" -> "application/json")
      )
    }
    println(mergedHeaders)
    Http(url.toString)
      .headers(mergedHeaders)
      .timeout(Int.MaxValue, Int.MaxValue)
      .compress(true)
      .put(payload)
      .asBytes
  }
}
