/*
 * Copyright The Microcks Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.microcks.operator.api.source.v1alpha1;

import io.github.microcks.operator.api.model.AdditionalPropertyPreserving;
import io.github.microcks.operator.api.model.MultiConditionsStatus;
import io.github.microcks.operator.api.model.Status;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.HashMap;
import java.util.Map;

/**
 * This the {@code status} of a {@link APISource} custom resource.
 * @author laurent
 */
public class APISourceStatus extends MultiConditionsStatus implements AdditionalPropertyPreserving {

   @JsonPropertyDescription("Global status of the reconciliation")
   private Status status = Status.UNKNOWN;

   private String message;

   @JsonPropertyDescription("Reconciled generation")
   private long observedGeneration;


   public Status getStatus() {
      return status;
   }

   public void setStatus(Status status) {
      this.status = status;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public long getObservedGeneration() {
      return observedGeneration;
   }

   public void setObservedGeneration(long observedGeneration) {
      this.observedGeneration = observedGeneration;
   }


   private Map<String, Object> additionalProperties;

   @Override
   public Map<String, Object> getAdditionalProperties() {
      return additionalProperties;
   }

   @Override
   public void setAdditionalProperty(String name, Object value) {
      if (this.additionalProperties == null) {
         this.additionalProperties = new HashMap<>(1);
      }
      this.additionalProperties.put(name, value);
   }
}