package org.hidetake.rss.aggregator.ntlm

import jcifs.ntlmssp.NtlmFlags
import jcifs.ntlmssp.Type1Message
import jcifs.ntlmssp.Type2Message
import jcifs.ntlmssp.Type3Message
import jcifs.util.Base64

import org.apache.http.impl.auth.NTLMEngine
import org.apache.http.impl.auth.NTLMEngineException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An implementation of NTLM engine.
 * 
 * @see http://hc.apache.org/httpcomponents-client-ga/ntlm.html
 * @author hidetake
 *
 */
class JCIFSEngine implements NTLMEngine {
  private static Logger logger = LoggerFactory.getLogger(JCIFSEngine)

  private static final int TYPE_1_FLAGS = (
  NtlmFlags.NTLMSSP_NEGOTIATE_56 |
  NtlmFlags.NTLMSSP_NEGOTIATE_128 |
  NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 |
  NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN |
  NtlmFlags.NTLMSSP_REQUEST_TARGET)

  String generateType1Msg(final String domain, final String workstation) throws NTLMEngineException {
    logger.debug('generating a type1 message for domain={}, workstation={}', domain, workstation)
    final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS, domain, workstation)
    Base64.encode(type1Message.toByteArray())
  }

  String generateType3Msg(final String username, final String password, final String domain, final String workstation, final String challenge) throws NTLMEngineException {
    logger.debug('generating a type2 message for username={}, password={}, domain={}, workstation={}, challenge={}',
        username, password, domain, workstation, challenge)
    Type2Message type2Message
    try {
      type2Message = new Type2Message(Base64.decode(challenge))
    } catch (final IOException exception) {
      throw new NTLMEngineException('Invalid NTLM type 2 message', exception)
    }
    final int type2Flags = type2Message.getFlags()
    final int type3Flags = type2Flags & (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER))
    logger.debug('generating a type3 message for username={}, password={}, domain={}, workstation={}, challenge={}, flags={}',
        username, password, domain, workstation, challenge, type3Flags)
    final Type3Message type3Message = new Type3Message(type2Message, password, domain, username, workstation, type3Flags)
    Base64.encode(type3Message.toByteArray())
  }
}
