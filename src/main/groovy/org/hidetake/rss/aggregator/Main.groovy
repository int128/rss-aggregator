package org.hidetake.rss.aggregator

import groovy.xml.MarkupBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder

import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.hidetake.rss.aggregator.ntlm.NTLMSchemeFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main class.
 * 
 * @author hidetake
 *
 */
class Main {
  private static String GENERATOR = 'RSS aggregator'
  private static Logger logger = LoggerFactory.getLogger(Main)

  /**
   * Fetches a resource as RSS object.
   * 
   * @param url resource
   * @return RSS object
   */
  static Object fetchAsRss(url) {
    def http = new HTTPBuilder(url)
    if (System.properties.basicUser) {
      assert System.properties.basicPassword != null
      http.client.credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
          System.properties.basicUser,
          System.properties.basicPassword))
      logger.info('basic authentication has been enabled for user {}', System.properties.basicUser)
    }
    if (System.properties.ntlmUser) {
      assert System.properties.ntlmPassword != null
      assert System.properties.ntlmWorkstation != null
      assert System.properties.ntlmDomain != null
      http.client.authSchemes.register('ntlm', new NTLMSchemeFactory())
      http.client.credentialsProvider.setCredentials(AuthScope.ANY, new NTCredentials(
          System.properties.ntlmUser,
          System.properties.ntlmPassword,
          System.properties.ntlmWorkstation,
          System.properties.ntlmDomain))
      logger.info('NTLM authentication has been enabled for user {}', System.properties.ntlmUser)
    }
    (http.get(contentType: ContentType.TEXT) as StringReader).withReader { reader ->
      // FIXME: skip first 1 byte to avoid UTF-8 BOM problem
      //reader.read()
      new XmlSlurper().parse(reader)
    }
  }

  /**
   * Aggregates RSS objects.
   * 
   * @param rssList
   * @param writer
   */
  static void aggregate(rssList, writer) {
    new MarkupBuilder(writer).rss(version: '2.0') {
      channel() {
        title(System.properties.outputXmlTitle)
        link(System.properties.outputXmlLink)
        description("aggregation of ${rssList.collect { rss -> rss.channel.title }.join(', ')}")
        lastBuildDate(new Date().toString())
        generator(GENERATOR)
        rssList.each { rss ->
          rss.channel.item.each { originalItem ->
            item() {
              title("${rss.channel.title}: ${originalItem.title}")
              link(originalItem.link)
              description(originalItem.description)
              author(originalItem.author)
              pubDate(originalItem.pubDate)
              guid(originalItem.guid)
            }
          }
        }
      }
    }
  }

  static void main(String[] urls) {
    assert System.properties.outputXmlPath != null
    new File(System.properties.outputXmlPath).withWriter('UTF-8') { writer ->
      aggregate(urls.collect { url -> fetchAsRss(url) }, writer)
    }
  }
}
