package com.ezconcurrent.utils;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * Created by sancheng on 12/7/2017.
 */
public class ConcurrentUtils {

    public static final int[] parallelBucketSort(int[] array, boolean threadAffinity)  {
        int parallel = Runtime.getRuntime().availableProcessors();
        if (parallel >= array.length) {Arrays.sort(array);return array;}
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        for (int e : array)  {
            if (e < min)  min = e;
            if (e > max)  max = e;
        }
        int block = (max - min) / parallel + 1 ;
        LinkedList<Integer>[] s = new LinkedList[parallel];
        for (int i = 0; i<array.length;i++)  {
            int k = (array[i] - min)/block;
            if (s[k] == null) {
                s[k] = new LinkedList<Integer>();
            }
            s[k].add(array[i]);
        }

        List<RecursiveAction> subActions = new LinkedList<>();
        for (int i = 0; i<s.length ;i++)  {
            final List a = s[i];
            if (a == null) continue;
            subActions.add(new RecursiveAction() {

                @Override
                protected void compute() {
                    Collections.sort(a);
                }
            });
        }

        ForkJoinTask.invokeAll(subActions);
        int i = 0;
        for(List<Integer> line : s) {
            if (line == null) continue;
            for (Integer num : line) {
                array[i++] = num;
            }
        }
        return array;
    }

    public static void main(String[] args) {
        int[] array = new int[]{7,7,8,5,2};
        ConcurrentUtils.parallelBucketSort(array,false);
       Arrays.stream(array).forEach(x->System.out.print(x + " "));
    }
}
