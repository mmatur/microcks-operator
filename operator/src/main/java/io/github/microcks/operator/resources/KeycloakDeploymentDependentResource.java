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

import io.github.microcks.operator.api.Microcks;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.jboss.logging.Logger;

/**
 * @author laurent
 */
@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=microcks-operator")
public class KeycloakDeploymentDependentResource extends CRUDKubernetesDependentResource<Deployment, Microcks> {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private static final String RESOURCE_SUFFIX = "-keycloak";

   public KeycloakDeploymentDependentResource() {
      super(Deployment.class);
   }

   public static final String getDeploymentName(Microcks microcks) {
      return microcks.getMetadata().getName() + RESOURCE_SUFFIX;
   }

   @Override
   protected Deployment desired(Microcks microcks, Context<Microcks> context) {
      logger.infof("Building desired Keycloak Deployment for '%s'", microcks.getMetadata().getName());

      final ObjectMeta microcksMetadata = microcks.getMetadata();
      final String microcksName = microcksMetadata.getName();

      Deployment deployment = ReconcilerUtils.loadYaml(Deployment.class, getClass(), "/k8s/keycloak-deployment.yml");
      deployment = new DeploymentBuilder(deployment)
            .editMetadata()
               .withName(getDeploymentName(microcks))
               .withNamespace(microcksMetadata.getNamespace())
               .addToLabels("app", microcksName)
               .addToLabels("app.kubernetes.io/name", getDeploymentName(microcks))
               .addToLabels("app.kubernetes.io/version", microcks.getSpec().getVersion())
               .addToLabels("app.kubernetes.io/part-of", microcksName)
            .endMetadata()
            .editSpec()
               .editSelector().addToMatchLabels("app", microcksName).endSelector()
               .editTemplate()
                  // make sure label selector matches label (which has to be matched by service selector too)
                  .editMetadata().addToLabels("app", microcksName).endMetadata()
                  .editSpec()
                     .editFirstContainer()
                        .withImage("quay.io/keycloak/keycloak:20.0.2")
                        .addNewEnv()
                           .withName("KEYCLOAK_ADMIN")
                           .withNewValueFrom()
                              .withNewSecretKeyRef()
                                 .withName(KeycloakSecretDependentResource.getSecretName(microcks))
                                 .withKey(KeycloakSecretDependentResource.KEYCLOAK_ADMIN_KEY)
                              .endSecretKeyRef()
                           .endValueFrom()
                        .endEnv()
                        .addNewEnv()
                           .withName("KEYCLOAK_ADMIN_PASSWORD")
                           .withNewValueFrom()
                              .withNewSecretKeyRef()
                                 .withName(KeycloakSecretDependentResource.getSecretName(microcks))
                                 .withKey(KeycloakSecretDependentResource.KEYCLOAK_ADMIN_PASSWORD_KEY)
                              .endSecretKeyRef()
                           .endValueFrom()
                        .endEnv()
                     .endContainer()
                  .endSpec()
               .endTemplate()
            .endSpec()
            .build();

      return deployment;
   }
}
