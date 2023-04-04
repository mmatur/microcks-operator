/*
 * Licensed to Laurent Broudoux (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.microcks.operator.resources;

import io.github.microcks.operator.MicrocksOperatorConfig;
import io.github.microcks.operator.api.Microcks;
import io.github.microcks.operator.model.NamedSecondaryResourceProvider;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.logging.Logger;

/**
 * A Keycloak Kubernetes Secret dependent resource.
 * @author laurent
 */
@KubernetesDependent(labelSelector = MicrocksOperatorConfig.RESOURCE_LABEL_SELECTOR)
public class KeycloakSecretDependentResource extends KubernetesDependentResource<Secret, Microcks>
      implements Creator<Secret, Microcks>, Deleter<Microcks>, NamedSecondaryResourceProvider<Microcks> {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   public static final String KEYCLOAK_ADMIN_KEY = "username";
   public static final String KEYCLOAK_ADMIN_PASSWORD_KEY = "password";

   public static final String DATABASE_USER_KEY = "postgresUsername";
   public static final String DATABASE_USER_PASSWORD_KEY = "postgresPassword";

   private static final String RESOURCE_SUFFIX = "-keycloak-admin";

   public KeycloakSecretDependentResource() {
      super(Secret.class);
   }

   public static final String getSecretName(Microcks microcks) {
      return microcks.getMetadata().getName() + RESOURCE_SUFFIX;
   }

   @Override
   public String getSecondaryResourceName(Microcks primary) {
      return primary.getMetadata().getName() + RESOURCE_SUFFIX;
   }

   @Override
   protected Secret desired(Microcks microcks, Context<Microcks> context) {
      logger.infof("Building desired Keycloak Secret for '%s'", microcks.getMetadata().getName());

      final ObjectMeta microcksMetadata = microcks.getMetadata();
      final String microcksName = microcksMetadata.getName();

      SecretBuilder builder = new SecretBuilder()
            .withNewMetadata()
               .withName(getSecondaryResourceName(microcks))
               .withNamespace(microcksMetadata.getNamespace())
               .addToLabels("app", microcksName)
               .addToLabels("container", "keycloak")
               .addToLabels("group", "microcks")
            .endMetadata()
            .addToStringData(KEYCLOAK_ADMIN_KEY, "admin" + RandomStringUtils.randomAlphanumeric(6))
            .addToStringData(KEYCLOAK_ADMIN_PASSWORD_KEY, RandomStringUtils.randomAlphanumeric(32))
            .addToStringData(DATABASE_USER_KEY, "user" + RandomStringUtils.randomAlphanumeric(6))
            .addToStringData(DATABASE_USER_PASSWORD_KEY, RandomStringUtils.randomAlphanumeric(32));

      return builder.build();
   }
}
