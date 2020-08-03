/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.common.util;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectReleaseTracker {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static Map<Object,String> OBJECTS = new ConcurrentHashMap<>(128, 0.75f, 2048);
  
  public static boolean track(Object object) {
    StringBuilderWriter sw = new StringBuilderWriter(4096);
    PrintWriter pw = new PrintWriter(sw);
    new ObjectTrackerException(object.getClass().getName()).printStackTrace(pw);
    OBJECTS.put(object, sw.toString());
    return true;
  }
  
  public static boolean release(Object object) {
    OBJECTS.remove(object);
    return true;
  }
  
  public static void clear() {
    OBJECTS.clear();
  }
  
  /**
   * @return null if ok else error message
   */
  public static String checkEmpty() {
    String error = null;
    Set<Entry<Object,String>> entries = OBJECTS.entrySet();

    if (entries.size() > 0) {
      List<String> objects = new ArrayList<>(entries.size());
      for (Entry<Object,String> entry : entries) {
        objects.add(entry.getKey().getClass().getSimpleName());
      }
      
      error = "ObjectTracker found " + entries.size() + " object(s) that were not released!!! " + objects + "\n";
      for (Entry<Object,String> entry : entries) {
        error += entry.getKey() + "\n" + entry.getValue() + "\n";
      }
    }
    
    return error;
  }
  
  public static class ObjectTrackerException extends RuntimeException {
    public ObjectTrackerException(String msg) {
      super(msg);
    }

    public ObjectTrackerException(Throwable t) {
      super(t);
    }
  }

}
