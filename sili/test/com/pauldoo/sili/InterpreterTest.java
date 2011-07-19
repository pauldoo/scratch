/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pauldoo.sili;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.experimental.theories.Theory;
import static org.junit.Assert.*;
import static com.pauldoo.sili.Interpreter.*;
import static com.pauldoo.sili.SExpression.*;

/**
 *
 * @author pauldoo
 */
public class InterpreterTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testEvalNumber() {
        assertEquals(
                new SExpression.Number(42),
                eval(new SExpression.Number(42), null));
    }
    
    @Test(expected=RuntimeException.class)
    public void testEvalUnboundVariable() {
        eval(new SExpression.Symbol("foo"), null);
    }

    @Test
    public void testEvalBoundVariable() {
        assertEquals(
            new SExpression.Number(6),
            eval(new SExpression.Symbol("foo"), new Pair(new Pair(new Symbol("foo"), new SExpression.Number(6)), null)));
    }
    
    @Test
    public void testLambda() {
        // (lambda (x) x)
        SExpression environment = new Pair(new Pair(new Symbol("foo"), new SExpression.Number(6)), null);
        SExpression expression = new Pair(Tag.Lambda, new Pair(new Pair(new Symbol("x"), null), new Pair(new Symbol("x"), null)));
        SExpression actual = Interpreter.eval(expression, environment);
        SExpression expected = new Pair(Tag.CompoundProcedure, new Pair(new Pair(new Symbol("x"), null), new Pair(new Symbol("x"), new Pair(environment, null))));
        assertEquals(expected, actual);
    }    
    
    @Test
    public void testApplyIdentity() {
        // ((lambda (x) x) 10)
        SExpression expression = new Pair(new Pair(Tag.Lambda, new Pair(new Pair(new Symbol("x"), null), new Pair(new Symbol("x"), null))), new Pair(new SExpression.Number(10), null));
        assertEquals(Interpreter.eval(expression, null), new SExpression.Number(10));
    }
    
    @Test
    public void testApplyReturnFirst() {
        // ((lambda (x y) x) 10 20)
        SExpression expression = new Pair(new Pair(Tag.Lambda, new Pair(new Pair(new Symbol("x"), new Pair(new Symbol("y"), null)), new Pair(new Symbol("x"), null))), new Pair(new SExpression.Number(10), new Pair(new SExpression.Number(20), null)));
        assertEquals(Interpreter.eval(expression, null), new SExpression.Number(10));
    }
    @Test
    public void testApplyReturnSecond() {
        // ((lambda (x y) y) 10 20)
        SExpression expression = new Pair(new Pair(Tag.Lambda, new Pair(new Pair(new Symbol("x"), new Pair(new Symbol("y"), null)), new Pair(new Symbol("y"), null))), new Pair(new SExpression.Number(10), new Pair(new SExpression.Number(20), null)));
        assertEquals(Interpreter.eval(expression, null), new SExpression.Number(20));
    }
    
    @Test
    public void testClosure() {
        //(((lambda (x) (lambda () x)) 10))
        
        SExpression inner = new Pair(Tag.Lambda, new Pair(null, new Pair(new Symbol("x"), null)));
        SExpression outer = new Pair(Tag.Lambda, new Pair( new Pair(new Symbol("x"), null), new Pair(inner, null)));
        SExpression expression = new Pair(new Pair(outer, new Pair(new SExpression.Number(30), null)), null);
        
        assertEquals(Interpreter.eval(expression, null), new SExpression.Number(30));
    }
}
