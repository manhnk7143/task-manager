/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author hieutrinh
 */
public class Stop {
   
    public static void main(String arg[]){
        Integer[] arr = {1,2,3,-5,2};
        isPossibleDivide(arr, 2);
    }

    public static boolean isPossibleDivide(Integer[] nums, int k) {
        Set<Integer> numsSet  = new HashSet(Arrays.asList(nums));
        Set<Integer> numsSet2  = new HashSet(Arrays.asList(nums));
        Iterator<Integer> it = numsSet2.iterator();
        while (!numsSet.isEmpty() && it.hasNext()){
            Integer item = it.next();
            if(!numsSet.contains(item)){
                continue;
            }
            for(int j = 0; j <= k; j ++){
                Integer nextItem = Integer.sum(item, j);
                if(!numsSet2.contains(nextItem)){
                    return false;
                }
                if(numsSet.contains(nextItem)){
                    numsSet.remove(nextItem);
                }
            }
        }
        return true;

    }
}
