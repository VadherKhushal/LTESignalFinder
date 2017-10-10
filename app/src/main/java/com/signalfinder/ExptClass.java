package com.signalfinder;

/**
 * Created by khushal.v on 25-09-2017.
 */

public class ExptClass {

    public static void main(String[] args) {
//        System.out.println(fin(5));
        fin(5);
    }

    private static void fin(int i) {
        String s = "";
        if(i>1){
            fin(i-1);
        }else{
            System.out.print( i+(i-1));
            return;
        }
    }
}
