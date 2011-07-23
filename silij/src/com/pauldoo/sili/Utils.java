/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pauldoo.sili;

import static com.pauldoo.sili.SExpression.*;

/**
 *
 * @author pauldoo
 */
public class Utils {
    private Utils()
    {
    }
    
    /**
        Helper to print typical lists (pairs where each 'second' is null or another pair).
    */
    public static String printList(SExpression exp)
    {
        if (exp == null) {
            return "()";
        }
        if (exp instanceof Pair) {
            return "(" + printListInner((Pair)exp) + ")";
        }
        return exp.toString();
    }
    
    private static String printListInner(Pair exp)
    {
        Pair second = (exp.second == null) ? null : ((Pair)exp.second) ;
        return printList(exp.first) + (((second) == null) ? "" : (" " + printListInner(second)));
    }
}
