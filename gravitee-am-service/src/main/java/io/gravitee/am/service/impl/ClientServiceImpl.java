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
package io.gravitee.am.service.impl;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.common.utils.SecureRandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.management.api.ClientRepository;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.ClientService;
import io.gravitee.am.service.exception.InvalidClientMetadataException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.*;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@Component
public class ClientServiceImpl implements ClientService {

    private final Logger LOGGER = LoggerFactory.getLogger(ClientServiceImpl.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Override
    public Maybe<Client> findById(String id) {
        LOGGER.debug("Find client by ID: {}", id);
        return applicationService.findById(id)
                .map(application -> {
                    Client client = convert(application);
                    // Send an empty array in case of no grant types
                    if (client.getAuthorizedGrantTypes() == null) {
                        client.setAuthorizedGrantTypes(Collections.emptyList());
                    }
                    return client;
                });
    }

    @Override
    public Maybe<Client> findByDomainAndClientId(String domain, String clientId) {
        LOGGER.debug("Find client by domain: {} and client id: {}", domain, clientId);
        return clientRepository.findByClientIdAndDomain(clientId, domain)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find client by domain: {} and client id: {}", domain, clientId, ex);
                    return Maybe.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find client by domain: %s and client id: %s", domain, clientId), ex));
                });
    }

    @Override
    public Single<Set<Client>> findByDomain(String domain) {
        LOGGER.debug("Find clients by domain", domain);
        return applicationService.findByDomain(domain, 0, Integer.MAX_VALUE)
                .map(pagedApplications -> pagedApplications.getData()
                        .stream()
                        .map(this::convert)
                        .collect(Collectors.toSet()));
    }

    @Override
    public Single<Set<Client>> search(String domain, String query) {
        LOGGER.debug("Search clients for domain {} and with query {}", domain, query);
        return applicationService.search(domain, query, 0, Integer.MAX_VALUE)
                .map(pagedApplications -> pagedApplications.getData()
                        .stream()
                        .map(this::convert)
                        .collect(Collectors.toSet()));
    }

    @Override
    public Single<Page<Client>> findByDomain(String domain, int page, int size) {
        LOGGER.debug("Find clients by domain", domain);
        return clientRepository.findByDomain(domain, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find clients by domain: {}", domain, ex);
                    return Single.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find clients by domain: %s", domain), ex));
                });
    }

    @Override
    public Single<Set<Client>> findByIdentityProvider(String identityProvider) {
        LOGGER.debug("Find clients by identity provider : {}", identityProvider);
        return clientRepository.findByIdentityProvider(identityProvider)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find clients by identity provider", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find clients by identity provider", ex));
                });
    }

    @Override
    public Single<Set<Client>> findByCertificate(String certificate) {
        LOGGER.debug("Find clients by certificate : {}", certificate);
        return clientRepository.findByCertificate(certificate)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find clients by certificate", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find clients by certificate", ex));
                });
    }

    @Override
    public Single<Set<Client>> findByDomainAndExtensionGrant(String domain, String extensionGrant) {
        LOGGER.debug("Find clients by domain {} and extension grant : {}", domain, extensionGrant);
        return clientRepository.findByDomainAndExtensionGrant(domain, extensionGrant)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find clients by extension grant", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find clients by extension grant", ex));
                });
    }

    @Override
    public Single<Set<Client>> findAll() {
        LOGGER.debug("Find clients");
        return applicationService.findAll(0, Integer.MAX_VALUE)
                .map(pagedApplications -> pagedApplications.getData()
                        .stream()
                        .map(this::convert)
                        .collect(Collectors.toSet()))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find clients", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find clients", ex));
                });
    }

    @Override
    public Single<Page<Client>> findAll(int page, int size) {
        LOGGER.debug("Find clients");
        return clientRepository.findAll(page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find clients", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find clients", ex));
                });
    }

    @Override
    public Single<Set<TopClient>> findTopClients() {
        LOGGER.debug("Find top clients");
        return clientRepository.findAll()
                .flatMapObservable(clients -> Observable.fromIterable(clients))
                .flatMapSingle(client -> accessTokenRepository.countByClientId(client.getClientId())
                        .map(oAuth2AccessTokens -> {
                            TopClient topClient = new TopClient();
                            topClient.setClient(client);
                            topClient.setAccessTokens(oAuth2AccessTokens);
                            return topClient;
                        }))
                .toList()
                .map(topClients -> topClients.stream().filter(topClient -> topClient.getAccessTokens() > 0).collect(Collectors.toSet()))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find top clients", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find top clients", ex));
                });
    }

    @Override
    public Single<Set<TopClient>> findTopClientsByDomain(String domain) {
        LOGGER.debug("Find top clients by domain: {}", domain);
        return clientRepository.findByDomain(domain)
                .flatMapObservable(clients -> Observable.fromIterable(clients))
                .flatMapSingle(client -> accessTokenRepository.countByClientId(client.getClientId())
                        .map(oAuth2AccessTokens -> {
                            TopClient topClient = new TopClient();
                            topClient.setClient(client);
                            topClient.setAccessTokens(oAuth2AccessTokens);
                            return topClient;
                        }))
                .toList()
                .map(topClients -> topClients.stream().filter(topClient -> topClient.getAccessTokens() > 0).collect(Collectors.toSet()))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find top clients by domain", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find top clients by domain", ex));
                });
    }

    @Override
    public Single<TotalClient> findTotalClientsByDomain(String domain) {
        LOGGER.debug("Find total clients by domain: {}", domain);
        return clientRepository.countByDomain(domain)
                .map(totalClients -> {
                    TotalClient totalClient = new TotalClient();
                    totalClient.setTotalClients(totalClients);
                    return totalClient;
                })
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find total clients by domain: {}", domain, ex);
                    return Single.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find total clients by domain: %s", domain), ex));
                });
    }

    @Override
    public Single<TotalClient> findTotalClients() {
        LOGGER.debug("Find total client");
        return clientRepository.count()
                .map(totalClients -> {
                    TotalClient totalClient = new TotalClient();
                    totalClient.setTotalClients(totalClients);
                    return totalClient;
                })
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find total clients", ex);
                    return Single.error(new TechnicalManagementException("An error occurs while trying to find total clients", ex));
                });
    }

    @Override
    public Single<Client> create(String domain, NewClient newClient, User principal) {
        LOGGER.debug("Create a new client {} for domain {}", newClient, domain);
        return applicationService.create(domain, convert(newClient), principal)
                .map(this::convert);
    }

    @Override
    public Single<Client> create(Client client) {
        LOGGER.debug("Create a client {} for domain {}", client, client.getDomain());

        if(client.getDomain()==null || client.getDomain().trim().isEmpty()) {
            return Single.error(new InvalidClientMetadataException("No domain set on client"));
        }

        /* openid response metadata */
        client.setId(RandomString.generate());
        //client_id & client_secret may be already informed if created through UI
        if(client.getClientId()==null) {
            client.setClientId(SecureRandomString.generate());
        }
        if(client.getClientSecret()==null || client.getClientSecret().trim().isEmpty()) {
            client.setClientSecret(SecureRandomString.generate());
        }
        if(client.getClientName()==null || client.getClientName().trim().isEmpty()) {
            client.setClientName("Unknown Client");
        }

        /* GRAVITEE.IO custom fields */
        client.setAccessTokenValiditySeconds(Client.DEFAULT_ACCESS_TOKEN_VALIDITY_SECONDS);
        client.setRefreshTokenValiditySeconds(Client.DEFAULT_REFRESH_TOKEN_VALIDITY_SECONDS);
        client.setIdTokenValiditySeconds(Client.DEFAULT_ID_TOKEN_VALIDITY_SECONDS);
        client.setEnabled(true);

        client.setCreatedAt(new Date());
        client.setUpdatedAt(client.getCreatedAt());

        return applicationService.create(convert(client))
                .map(this::convert);
    }

    @Override
    public Single<Client> update(Client client) {
        LOGGER.debug("Update client_id {} for domain {}", client.getClientId(), client.getDomain());

        if(client.getDomain()==null || client.getDomain().trim().isEmpty()) {
            return Single.error(new InvalidClientMetadataException("No domain set on client"));
        }

        return applicationService.update(convert(client))
                .map(this::convert);
    }

    @Override
    public Single<Client> patch(String domain, String id, PatchClient patchClient, boolean forceNull, User principal) {
        LOGGER.debug("Patch a client {} for domain {}", id, domain);
        return applicationService.patch(domain, id, convert(patchClient), principal)
                .map(this::convert);
    }

    @Override
    public Completable delete(String clientId, User principal) {
        LOGGER.debug("Delete client {}", clientId);
        return applicationService.delete(clientId, principal);
    }

    @Override
    public Single<Client> renewClientSecret(String domain, String id, User principal) {
        LOGGER.debug("Renew client secret for client {} in domain {}", id, domain);
        return applicationService.renewClientSecret(domain, id, principal)
                .map(this::convert);
    }

    private NewClient convert(NewApplication newApplication) {
        return null;
    }
    private NewApplication convert(NewClient newClient) {
        return null;
    }
    private PatchClient convert(PatchApplication patchApplication) {
        return null;
    }
    private PatchApplication convert(PatchClient patchClient) {
        return null;
    }
    private Client convert(Application application) {
        return null;
    }
    private Application convert(Client client) {
        return null;
    }
}
