/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.index.hashindex.local.cache;

import java.util.concurrent.atomic.AtomicInteger;

import com.orientechnologies.common.directmemory.ODirectMemory;
import com.orientechnologies.common.directmemory.ODirectMemoryFactory;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OLogSequenceNumber;

/**
 * @author Andrey Lomakin
 * @since 05.08.13
 */
public class OCachePointer {
  private final ODirectMemory         directMemory   = ODirectMemoryFactory.INSTANCE.directMemory();

  private final AtomicInteger         referrersCount = new AtomicInteger();
  private final AtomicInteger         usagesCounter  = new AtomicInteger();

  private volatile OLogSequenceNumber lastFlushedLsn;

  private final long                  dataPointer;

  public OCachePointer(long dataPointer, OLogSequenceNumber lastFlushedLsn) {
    this.lastFlushedLsn = lastFlushedLsn;
    this.dataPointer = dataPointer;
  }

  public OCachePointer(byte[] data, OLogSequenceNumber lastFlushedLsn) {
    this.lastFlushedLsn = lastFlushedLsn;
    dataPointer = directMemory.allocate(data);
  }

  public OLogSequenceNumber getLastFlushedLsn() {
    return lastFlushedLsn;
  }

  public void setLastFlushedLsn(OLogSequenceNumber lastFlushedLsn) {
    this.lastFlushedLsn = lastFlushedLsn;
  }

  public void incrementReferrer() {
    referrersCount.incrementAndGet();
  }

  public void decrementReferrer() {
    if (referrersCount.decrementAndGet() == 0) {
      directMemory.free(dataPointer);
    }
  }

  public void incrementUsages() {
    usagesCounter.incrementAndGet();
  }

  public void decrementUsages() {
    usagesCounter.decrementAndGet();
  }

  public int getUsagesCount() {
    return usagesCounter.get();
  }

  public long getDataPointer() {
    return dataPointer;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();

    if (referrersCount.get() > 0)
      directMemory.free(dataPointer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OCachePointer that = (OCachePointer) o;

    if (dataPointer != that.dataPointer)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (int) (dataPointer ^ (dataPointer >>> 32));
  }

  @Override
  public String toString() {
    return "OCachePointer{" + "referrersCount=" + referrersCount + ", usagesCounter=" + usagesCounter + ", dataPointer="
        + dataPointer + '}';
  }
}
