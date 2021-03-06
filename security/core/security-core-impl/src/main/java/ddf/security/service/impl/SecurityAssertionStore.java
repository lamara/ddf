/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package ddf.security.service.impl;

import ddf.security.assertion.SecurityAssertion;
import ddf.security.assertion.impl.SecurityAssertionImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStoreFactory;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.SAMLTokenPrincipal;

/**
 * Creates a SecurityAssertion object by extracting the token id from the message
 * and using that id to retrieve the SecurityToken object from the TokenStore instance.
 * @author tustisos
 *
 */
public final class SecurityAssertionStore
{
    /**
     * Return the SecurityAssertion wrapper associated with the provided message
     * @param message Message
     * @return SecurityAssertion
     */
    public static SecurityAssertion getSecurityAssertion(Message message)
    {
        if(message != null)
        {
            TokenStore tokenStore = getTokenStore(message);
            SAMLTokenPrincipal principal = (SAMLTokenPrincipal) message.get(WSS4JInInterceptor.PRINCIPAL_RESULT);
            if(tokenStore != null && principal != null)
            {
                SecurityToken token = tokenStore.getToken(principal.getId());
                if(token != null)
                {
                    return new SecurityAssertionImpl(token);
                }
            }
        }
        return new SecurityAssertionImpl();
    }

    /**
     * Return the TokenStore associated with this message.
     * @param message
     * @return TokenStore
     */
    private static TokenStore getTokenStore(Message message)
    {
        EndpointInfo info = message.getExchange().get(Endpoint.class).getEndpointInfo();
        synchronized (info)
        {
            TokenStore tokenStore = (TokenStore) message.getContextualProperty(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE);
            if (tokenStore == null)
            {
                tokenStore = (TokenStore) info.getProperty(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE);
            }
            if (tokenStore == null)
            {
                TokenStoreFactory tokenStoreFactory = TokenStoreFactory.newInstance();
                tokenStore = tokenStoreFactory.newTokenStore(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, message);
                info.setProperty(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, tokenStore);
            }
            return tokenStore;
        }
    }
}
