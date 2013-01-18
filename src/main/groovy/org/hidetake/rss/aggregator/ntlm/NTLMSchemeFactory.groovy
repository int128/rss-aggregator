package org.hidetake.rss.aggregator.ntlm

import org.apache.http.auth.AuthScheme
import org.apache.http.auth.AuthSchemeFactory
import org.apache.http.impl.auth.NTLMScheme
import org.apache.http.params.HttpParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An implementation of authentication scheme factory for NTLM.
 * 
 * @see http://hc.apache.org/httpcomponents-client-ga/ntlm.html
 * @author hidetake
 *
 */
class NTLMSchemeFactory implements AuthSchemeFactory {
  private static Logger logger = LoggerFactory.getLogger(NTLMSchemeFactory)

  AuthScheme newInstance(final HttpParams params) {
    logger.debug("creating an new instance with params: {}", params.toString())
    new NTLMScheme(new JCIFSEngine())
  }
}
