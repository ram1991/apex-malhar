/*
 * Copyright (c) 2013 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.algo;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.annotation.InputPortFieldAnnotation;
import com.datatorrent.api.annotation.OperatorAnnotation;
import com.datatorrent.api.annotation.OutputPortFieldAnnotation;
import com.datatorrent.lib.util.AbstractBaseFrequentKey;
import com.datatorrent.lib.util.UnifierArrayHashMapFrequent;
import com.datatorrent.lib.util.UnifierHashMapFrequent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * <p>
 * This operator filters the incoming stream of values by emitting the value or values (if there is a tie)
 * that occurred the largest number of times within each window to the output port "list".&nbsp;
 * One of the values is emitted to the output port "least" at the end of each window.
 * <p>
 * Occurrences of each tuple is counted and at the end of window any of the most frequent tuple is emitted on output port least and all least frequent
 * tuples on output port list<p>
 * This module is an end of window module<br>
 * In case of a tie any of the least key would be emitted. The list port would however have all the tied keys
 * <br>
 *  <b>StateFull : Yes</b>, Values are compared all over  application window can be > 1. <br>
 *  <b>Partitions : Yes</b>, Result is unified on output port. <br>
 * <br>
 * <b>Ports</b>:<br>
 * <b>data</b>: expects K<br>
 * <b>most</b>: emits HashMap&lt;K,Integer&gt;(1), Where K is the least occurring key in the window. In case of tie any of the least key would be emitted<br>
 * <b>list</b>: emits ArrayList&lt;HashMap&lt;K,Integer&gt;(1)&gt, Where the list includes all the keys that are least frequent<br>
 * <br>
 * <b>Properties</b>: None<br>
 * <br>
 * <b>Compile time checks</b>: None<br>
 * <b>Specific run time checks</b>: None<br>
 * <br>
 *
 * @displayName Emit Most Frequent Value
 * @category algorithm
 * @tags filter, count
 *
 * @since 0.3.2
 */

@OperatorAnnotation(partitionable = true)
public class MostFrequentKey<K> extends AbstractBaseFrequentKey<K>
{
  @InputPortFieldAnnotation(name = "data")
  public final transient DefaultInputPort<K> data = new DefaultInputPort<K>()
  {
    /**
     * Calls super.processTuple(tuple)
     */
    @Override
    public void process(K tuple)
    {
      processTuple(tuple);
    }
  };
  @OutputPortFieldAnnotation(name = "most")
  public final transient DefaultOutputPort<HashMap<K, Integer>> most = new DefaultOutputPort<HashMap<K, Integer>>()
  {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Unifier<HashMap<K, Integer>> getUnifier()
    {
      UnifierHashMapFrequent ret = new UnifierHashMapFrequent<K>();
      ret.setLeast(false);
      return ret;
    }
  };


  @OutputPortFieldAnnotation(name = "list")
  public final transient DefaultOutputPort<ArrayList<HashMap<K, Integer>>> list = new DefaultOutputPort<ArrayList<HashMap<K, Integer>>>()
  {
    @SuppressWarnings("rawtypes")
    @Override
    public Unifier<ArrayList<HashMap<K, Integer>>> getUnifier()
    {
      Unifier<ArrayList<HashMap<K, Integer>>> ret = new UnifierArrayHashMapFrequent<K>();
      ((UnifierHashMapFrequent) ret).setLeast(false);
      return ret;
    }
  };

  /**
   * Emits tuple on port "most"
   * @param tuple
   */
  @Override
  public void emitTuple(HashMap<K, Integer> tuple)
  {
    most.emit(tuple);
  }

  /**
   * Emits tuple on port "list"
   * @param tlist
   */
  @Override
  public void emitList(ArrayList<HashMap<K, Integer>> tlist)
  {
    list.emit(tlist);
  }

  /**
   * returns val1 < val2
   * @param val1
   * @param val2
   * @return val1 > val2
   */
  @Override
  public boolean compareCount(int val1, int val2)
  {
    return val1 > val2;
  }
}
