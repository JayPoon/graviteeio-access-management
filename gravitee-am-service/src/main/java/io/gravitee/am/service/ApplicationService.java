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
package io.gravitee.am.service;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.model.NewApplication;
import io.gravitee.am.service.model.PatchApplication;
import io.gravitee.am.service.model.TopApplication;
import io.gravitee.am.service.model.TopClient;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.Set;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ApplicationService {

    Single<Page<Application>> findAll(int page, int size);

    Single<Page<Application>> findByDomain(String domain, int page, int size);

    Single<Page<Application>> search(String domain, String query, int page, int size);

    Maybe<Application> findById(String id);

    Single<Application> create(String domain, NewApplication newApplication, User principal);

    Single<Application> create(Application application);

    Single<Application> update(Application application);

    Single<Application> patch(String domain, String id, PatchApplication patchApplication, User principal);

    Single<Application> renewClientSecret(String domain, String id, User principal);

    Completable delete(String id, User principal);

    Single<Long> count();

    Single<Long> countByDomain(String domainId);

    Single<Set<TopApplication>> findTopApplications();

    Single<Set<TopApplication>> findTopApplicationsByDomain(String domain);

    default Single<Application> create(String domain, NewApplication newApplication) {
        return create(domain, newApplication, null);
    }

    default Single<Application> patch(String domain, String id, PatchApplication patchApplication) {
        return patch(domain, id, patchApplication, null);
    }

    default Single<Application> renewClientSecret(String domain, String id) {
        return renewClientSecret(domain, id, null);
    }

    default Completable delete(String id) {
        return delete(id, null);
    }
}
