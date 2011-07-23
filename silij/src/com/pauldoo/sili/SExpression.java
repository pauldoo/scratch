/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pauldoo.sili;

/**
 *
 * @author pauldoo
 */
public interface SExpression {
    public static class Number implements SExpression
    {
        public final long value;

        public Number(long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

        @Override
        public boolean equals(Object exp) {
            return (exp == this) || 
                   ((exp instanceof Number) && ((Number)exp).value == this.value);
        }
    }
    
    public static enum Tag implements SExpression {
        Lambda,
        CompoundProcedure;
    }
    
    public static class Symbol implements SExpression 
    {
        public final String name;

        public Symbol(String name) {
            this.name = name.intern();
        }

        @Override
        public String toString() {
            return name;
        }
        
        @Override
        public boolean equals(Object exp) {
            return (exp == this) ||
                    ((exp instanceof Symbol) && ((Symbol)exp).name.equals(this.name));
        }        
   }
    
    public static class Pair implements SExpression
    {
        public final SExpression first;
        public final SExpression second;

        public Pair(SExpression first, SExpression second) {
            this.first = first;
            this.second = second;
            // TODO: Introduce proper nil value in place of Java's null?
        }

        @Override
        public String toString() {
            return
                    "<" + ((first == null) ? "null" : first.toString()) + ", " + ((second == null) ? "null" : second.toString()) + ">";
        }
        
        @Override
        public boolean equals(Object exp) {
            return (exp == this) ||
                    ((exp instanceof Pair) && equals(((Pair)exp).first, this.first) && equals(((Pair)exp).second, this.second));
        }
        
        private static boolean equals(SExpression a00, SExpression b00)
        {
            return
                    a00 == b00 ||
                    (a00 != null && b00 != null && a00.equals(b00));
        }
    }
}
