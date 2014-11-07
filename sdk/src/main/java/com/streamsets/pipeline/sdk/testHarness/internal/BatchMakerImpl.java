package com.streamsets.pipeline.sdk.testharness.internal;

import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Record;

import java.util.*;

/**
 * Implementation of BatchMaker interface for the test harness
 */
public class BatchMakerImpl implements BatchMaker {

  private final Set<String> outputLanes;
  private final Map<String, List<Record>> laneToRecordsMap;
  private final String singleLaneOutput;

  public BatchMakerImpl(Set<String> outputlanes) {
    outputLanes = outputlanes;
    if(outputlanes.size() == 1) {
      singleLaneOutput = outputlanes.iterator().next();
    } else {
      singleLaneOutput = null;
    }
    laneToRecordsMap = new HashMap<String, List<Record>>();
  }

  @Override
  public Set<String> getLanes() {
    return outputLanes;
  }

  @Override
  public void addRecord(Record record, String... lanes) {
    if(lanes == null || lanes.length == 0) {
      List<Record> records = laneToRecordsMap.get(singleLaneOutput);
      if(records == null) {
        records = new ArrayList<Record>();
        laneToRecordsMap.put(singleLaneOutput, records);
      }
      records.add(record);
      return;
    }
    for(String lane : lanes) {
      List<Record> records = laneToRecordsMap.get(lane);
      if(records == null) {
        records = new ArrayList<Record>();
        laneToRecordsMap.put(lane, records);
      }
      records.add(record);
    }
  }

  /**
   * Returns the contents of records present in this batch maker as
   * a Map of lane names and the corresponding records
   * @return
   */
  public Map<String, List<Record>> getLaneToRecordsMap() {
    return laneToRecordsMap;
  }
}