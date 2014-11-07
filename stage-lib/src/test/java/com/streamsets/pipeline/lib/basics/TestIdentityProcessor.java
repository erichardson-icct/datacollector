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
package com.streamsets.pipeline.lib.basics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.streamsets.pipeline.api.*;
import com.streamsets.pipeline.sdk.testharness.Constants;
import com.streamsets.pipeline.sdk.testharness.ProcessorRunner;
import com.streamsets.pipeline.sdk.testharness.RecordProducer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

public class TestIdentityProcessor {

  private static final String DEFAULT_LANE = "lane";

  @Test
  public void testProcessor() throws Exception {
    Record record1 = Mockito.mock(Record.class);
    Record record2 = Mockito.mock(Record.class);
    Batch batch = Mockito.mock(Batch.class);
    Mockito.when(batch.getRecords()).thenReturn(ImmutableSet.of(record1, record2).iterator());
    BatchMaker batchMaker = Mockito.mock(BatchMaker.class);

    Processor identity = new IdentityProcessor();
    identity.process(batch, batchMaker);

    ArgumentCaptor<Record> recordCaptor = ArgumentCaptor.forClass(Record.class);
    ArgumentCaptor<String> laneCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(batchMaker, Mockito.times(2)).addRecord(recordCaptor.capture(), laneCaptor.capture());

    Assert.assertEquals(ImmutableList.of(record1, record2), recordCaptor.getAllValues());
   // Assert.assertEquals(ImmutableList.of(null, null), laneCaptor.getAllValues());
  }

  @Test
  public void testIdentityProcDefaultConfig() throws StageException {
    //Build record producer
    RecordProducer rp = new RecordProducer();
    rp.addFiled("transactionLog", RecordProducer.Type.STRING);
    rp.addFiled("transactionFare", RecordProducer.Type.DOUBLE);
    rp.addFiled("transactionDay", RecordProducer.Type.INTEGER);

    Map<String, List<Record>> result = new ProcessorRunner.Builder<IdentityProcessor>(rp)
      .addProcessor(IdentityProcessor.class)
      .build()
      .run();

    Assert.assertNotNull(result);
    Assert.assertNotNull(result.get(DEFAULT_LANE));
    Assert.assertTrue(result.get(DEFAULT_LANE).size() == 10);
  }

  @Test
  public void testIdentityProc() throws StageException {
    //Build record producer
    RecordProducer rp = new RecordProducer();
    rp.addFiled("transactionLog", RecordProducer.Type.STRING);
    rp.addFiled("transactionFare", RecordProducer.Type.DOUBLE);
    rp.addFiled("transactionDay", RecordProducer.Type.INTEGER);

    Map<String, List<Record>> result = new ProcessorRunner.Builder<IdentityProcessor>(rp)
      .addProcessor(IdentityProcessor.class)
      .maxBatchSize(35)
      .outputLanes(ImmutableSet.of("outputLane"))
      .sourceOffset(Constants.DEFAULT_SOURCE_OFFSET)
      .build()
      .run();

    Assert.assertNotNull(result);

    //No records in default lane
    Assert.assertNull(result.get(DEFAULT_LANE));
    //All records in the configured lane
    Assert.assertNotNull(result.get("outputLane"));
    Assert.assertTrue(result.get("outputLane").size() == 35);
  }


}
