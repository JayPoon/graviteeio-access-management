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
package io.gravitee.am.management.repository.proxy;

import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.model.AccessToken;
import io.gravitee.am.repository.oauth2.model.AccessTokenCriteria;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.springframework.stereotype.Component;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class AccessTokenRepositoryProxy extends AbstractProxy<AccessTokenRepository> implements AccessTokenRepository {

    public Maybe<AccessToken> findByToken(String token) {
        return target.findByToken(token);
    }

    public Single<AccessToken> create(AccessToken accessToken) {
        return target.create(accessToken);
    }

    public Completable delete(String token) {
        return target.delete(token);
    }

    public Observable<AccessToken> findByClientIdAndSubject(String clientId, String subject) {
        return target.findByClientIdAndSubject(clientId, subject);
    }

    public Observable<AccessToken> findByClientId(String clientId) {
        return target.findByClientId(clientId);
    }

    @Override
    public Maybe<AccessToken> findByCriteria(AccessTokenCriteria accessTokenCriteria) {
        return target.findByCriteria(accessTokenCriteria);
    }
}
