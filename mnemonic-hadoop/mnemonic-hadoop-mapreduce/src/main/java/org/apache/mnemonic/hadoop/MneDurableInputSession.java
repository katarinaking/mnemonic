/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mnemonic.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.mnemonic.ConfigurationException;
import org.apache.mnemonic.DurableType;
import org.apache.mnemonic.NonVolatileMemAllocator;
import org.apache.mnemonic.Utils;
import org.apache.mnemonic.sessions.DurableInputSession;

public class MneDurableInputSession<V>
    extends DurableInputSession<V, NonVolatileMemAllocator> {

  private TaskAttemptContext taskAttemptContext;
  private Configuration configuration;

  public MneDurableInputSession(TaskAttemptContext taskAttemptContext) {
    this(taskAttemptContext.getConfiguration());
    setTaskAttemptContext(taskAttemptContext);
  }

  public MneDurableInputSession(Configuration configuration) {
    setConfiguration(configuration);
  }

  public void validateConfig() {
    if (getDurableTypes().length < 1) {
      throw new ConfigurationException("The durable type of record parameters does not exist");
    } else {
      if (DurableType.DURABLE == getDurableTypes()[0]
          && getEntityFactoryProxies().length < 1) {
        throw new ConfigurationException("The durable entity proxy of record parameters does not exist");
      }
    }
  }

  public void readConfig(String prefix) {
    Configuration conf = getConfiguration();
    if (conf == null) {
      throw new ConfigurationException("Configuration has not yet been set");
    }
    setServiceName(MneConfigHelper.getMemServiceName(conf, MneConfigHelper.DEFAULT_INPUT_CONFIG_PREFIX));
    setDurableTypes(MneConfigHelper.getDurableTypes(conf, MneConfigHelper.DEFAULT_INPUT_CONFIG_PREFIX));
    setEntityFactoryProxies(Utils.instantiateEntityFactoryProxies(
        MneConfigHelper.getEntityFactoryProxies(conf, MneConfigHelper.DEFAULT_INPUT_CONFIG_PREFIX)));
    setSlotKeyId(MneConfigHelper.getSlotKeyId(conf, MneConfigHelper.DEFAULT_INPUT_CONFIG_PREFIX));
    validateConfig();
  }

  public void initialize(Path path) {
    m_act = new NonVolatileMemAllocator(Utils.getNonVolatileMemoryAllocatorService(getServiceName()), 1024000L,
        path.toString(), true);
    m_handler = m_act.getHandler(getSlotKeyId());
  }

  public TaskAttemptContext getTaskAttemptContext() {
    return taskAttemptContext;
  }

  public void setTaskAttemptContext(TaskAttemptContext taskAttemptContext) {
    this.taskAttemptContext = taskAttemptContext;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}