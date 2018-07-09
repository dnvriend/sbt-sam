package com.github.dnvriend.comprehend

import com.amazonaws.services.comprehend.model._
import com.amazonaws.services.comprehend.{ AmazonComprehend, AmazonComprehendClientBuilder }

import scala.collection.JavaConverters._

/**
 * Amazon Comprehend uses natural language processing (NLP) to extract insights about the content of documents.
 * Amazon Comprehend processes any text file in UTF-8 format. It develops insights by recognizing the entities,
 * key phrases, language, sentiments, and other common elements in a document.
 *
 * Use Amazon Comprehend to create new products based on understanding the structure of documents.
 * For example, using Amazon Comprehend you can search social networking feeds for mentions of products
 * or scan an entire document repository for key phrases.
 */
object Comprehend {
  def client(): AmazonComprehend = {
    AmazonComprehendClientBuilder.defaultClient()
  }

  /**
   * Determines the dominant language of the input text.
   */
  def detectDominantLanguage(
    text: String,
    client: AmazonComprehend
  ): List[DominantLanguage] = {
    client.detectDominantLanguage(
      new DetectDominantLanguageRequest().withText(text)
    ).getLanguages.asScala.toList
  }

  /**
   * Result of the DetectSentiment operation returned by the service.
   */
  def detectSentiment(
    text: String,
    languageCode: String,
    client: AmazonComprehend
  ): DetectSentimentResult = {
    client.detectSentiment(
      new DetectSentimentRequest()
        .withText(text)
        .withLanguageCode(LanguageCode.fromValue(languageCode))
    )
  }

  /**
   * Result of the DetectKeyPhrases operation returned by the service.
   */
  def detectKeyPhrases(
    text: String,
    languageCode: String,
    client: AmazonComprehend
  ): List[KeyPhrase] = {
    client.detectKeyPhrases(
      new DetectKeyPhrasesRequest()
        .withText(text)
        .withLanguageCode(LanguageCode.fromValue(languageCode))
    ).getKeyPhrases.asScala.toList
  }

  /**
   * Result of the DetectEntities operation returned by the service.
   */
  def detectEntities(
    text: String,
    languageCode: String,
    client: AmazonComprehend
  ): List[Entity] = {
    client.detectEntities(
      new DetectEntitiesRequest()
        .withText(text)
        .withLanguageCode(LanguageCode.fromValue(languageCode))
    ).getEntities.asScala.toList
  }
}
