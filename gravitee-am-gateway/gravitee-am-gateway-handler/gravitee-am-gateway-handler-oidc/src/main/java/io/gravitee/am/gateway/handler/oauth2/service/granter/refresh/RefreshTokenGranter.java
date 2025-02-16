/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.oauth2.service.granter.refresh;

import io.gravitee.am.common.oauth2.GrantType;
import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.gateway.handler.common.auth.UserAuthenticationManager;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.service.granter.AbstractTokenGranter;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequestResolver;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.model.Client;
import io.gravitee.am.model.User;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the Refresh Token Grant Flow
 * See <a href="https://tools.ietf.org/html/rfc6749#section-6">6. Refreshing an Access Token</a>
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RefreshTokenGranter extends AbstractTokenGranter {

    private UserAuthenticationManager userAuthenticationManager;

    public RefreshTokenGranter() {
        super(GrantType.REFRESH_TOKEN);
    }

    public RefreshTokenGranter(TokenRequestResolver tokenRequestResolver, TokenService tokenService, UserAuthenticationManager userAuthenticationManager) {
        this();
        setTokenRequestResolver(tokenRequestResolver);
        setTokenService(tokenService);
        this.userAuthenticationManager = userAuthenticationManager;
    }

    @Override
    protected Single<TokenRequest> parseRequest(TokenRequest tokenRequest, Client client) {
        String refreshToken = tokenRequest.parameters().getFirst("refresh_token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Single.error(new InvalidRequestException("A refresh token must be supplied."));
        }

        return super.parseRequest(tokenRequest, client)
                .flatMap(tokenRequest1 -> getTokenService().refresh(refreshToken, tokenRequest, client)
                        .map(refreshToken1 -> {
                            // set resource owner
                            if (refreshToken1.getSubject() != null) {
                                tokenRequest1.setSubject(refreshToken1.getSubject());
                            }
                            // set scopes
                            // The requested scope MUST NOT include any scope
                            // not originally granted by the resource owner, and if omitted is
                            // treated as equal to the scope originally granted by the resource owner.
                            final Set<String> originalScopes = (refreshToken1.getScope() != null ? new HashSet(Arrays.asList(refreshToken1.getScope().split("\\s+"))) : null);
                            final Set<String> requestedScopes = tokenRequest1.getScopes();
                            if (requestedScopes == null || requestedScopes.isEmpty()) {
                                tokenRequest1.setScopes(originalScopes);
                            } else if (originalScopes != null && !originalScopes.isEmpty()) {
                                Set<String> filteredScopes = requestedScopes
                                        .stream()
                                        .filter(requestedScope -> originalScopes.contains(requestedScope))
                                        .collect(Collectors.toSet());
                                tokenRequest1.setScopes(filteredScopes);
                            }
                            return tokenRequest1;
                        }));
    }

    @Override
    protected Maybe<User> resolveResourceOwner(TokenRequest tokenRequest, Client client) {
        final String subject = tokenRequest.getSubject();

        if (subject == null) {
            return Maybe.empty();
        }

        return userAuthenticationManager.loadUserByUsername(subject)
                .onErrorResumeNext(ex -> { return Maybe.error(new InvalidGrantException()); });
    }

    @Override
    protected Single<TokenRequest> resolveRequest(TokenRequest tokenRequest, Client client, User endUser) {
        // request has already been resolved during parse request step
        return Single.just(tokenRequest);
    }
}
