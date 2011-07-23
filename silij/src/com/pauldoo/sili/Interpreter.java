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
public class Interpreter {
    private Interpreter() {}
    
    public static SExpression eval(SExpression exp, SExpression env)
    {
        if (isSelfEvaluating(exp)) {
            return exp;
        } else if (isVariable(exp)) {
            return lookupVariableValue(exp, env);
        } else if (isLambda(exp)) {
            return
                makeProcedure(lambdaParameters(exp), lambdaBody(exp), env);
        } else if (isApplication(exp)) {
            return
                apply(
                    eval(operator(exp), env),
                    listOfValues(operands(exp), env));
        } else {
            throw new RuntimeException();
        }
    }
    
    private static boolean isCompoundProcedure(SExpression exp)
    {
        return
            isPair(exp) &&
            Tag.CompoundProcedure.equals(first(exp));        
    }
    
    private static SExpression apply(SExpression procedure, SExpression arguments)
    {
        if (isCompoundProcedure(procedure)) {
            return eval(
                    procedureBody(procedure),
                    extendEnvironment(
                        procedureParameters(procedure),
                        arguments,
                        procedureEnvironment(procedure)));
        } else {
            throw new RuntimeException("Invalid procedure '" + procedure + "'");
        }
    }
    
    private static SExpression extendEnvironment(
        SExpression parameterNames, 
        SExpression parameterValues, 
        SExpression baseEnv)
    {
        if (parameterNames == null && parameterValues == null) {
            return baseEnv;
        }
        if (parameterNames == null) {
            throw new RuntimeException("Too many arguments");
        }
        if (parameterValues == null) {
            throw new RuntimeException("Too few arguments");
        }
        
        return new Pair(
                new Pair(
                    (Symbol)first(parameterNames),
                    first(parameterValues)),
                extendEnvironment(
                    second(parameterNames),
                    second(parameterValues),
                    baseEnv));
    }
    
    private static boolean isApplication(SExpression exp)
    {
        return isPair(exp);
    }
    
    private static boolean isSelfEvaluating(SExpression exp)
    {
        return exp instanceof SExpression.Number;
    }
    
    private static boolean isVariable(SExpression exp)
    {
        return exp instanceof Symbol;
    }
    
    private static SExpression lookupVariableValue(SExpression exp, SExpression env)
    {
        if (!isVariable(exp)) {
            throw new RuntimeException("Not a variable '" + exp + "'");
        }
        if (env == null) {
            throw new RuntimeException("Unbound variable '" + exp + "'");
        }
        if (!isPair(env)) {
            throw new RuntimeException("Not an environment");
        }
        
        String nameToResolve = ((Symbol)exp).name;
        String nameInEnvironment = ((Symbol)first(first(env))).name;
        if (nameInEnvironment.equals(nameToResolve)) {
            return second(first(env));
        } else {
            return lookupVariableValue(exp, second(env));
        }
    }
    
    private static boolean isPair(SExpression exp)
    {
        return exp instanceof Pair;
    }
    
    private static SExpression first(SExpression exp)
    {
        return ((Pair)exp).first;
    }
    
    private static SExpression second(SExpression exp)
    {
        return ((Pair)exp).second;
    }
    
    private static boolean isLambda(SExpression exp)
    {
        return
            isPair(exp) &&
            Tag.Lambda.equals(first(exp));
    }
    
    private static SExpression lambdaParameters(SExpression exp) {
        if (!isLambda(exp)) {
            throw new RuntimeException("Not a lambda '" + exp + "'");
        }
        return first(second(exp));
    }
    
    private static SExpression lambdaBody(SExpression exp) {
        if (!isLambda(exp)) {
            throw new RuntimeException("Not a lambda '" + exp + "'");
        }        
        return first(second(second(exp)));
    }
    
    private static SExpression makeProcedure(SExpression parameters, SExpression body, SExpression env)
    {
        return
            new Pair(Tag.CompoundProcedure,
                new Pair(parameters,
                    new Pair(body,
                        new Pair(env,
                            null))));
    }
    
    private static SExpression procedureParameters(SExpression exp)
    {
        if (!isCompoundProcedure(exp)) {
            throw new RuntimeException("Not a compund procedure '" + exp + "'");
        }   
        return first(second(exp));
    }
    
    private static SExpression procedureBody(SExpression exp)
    {
        if (!isCompoundProcedure(exp)) {
            throw new RuntimeException("Not a compund procedure '" + exp + "'");
        }   
        return first(second(second(exp)));
    }

    private static SExpression procedureEnvironment(SExpression exp)
    {
        if (!isCompoundProcedure(exp)) {
            throw new RuntimeException("Not a compund procedure '" + exp + "'");
        }   
        return first(second(second(second(exp))));
    }
    
    private static SExpression operator(SExpression exp)
    {
        if (!isApplication(exp)) {
            throw new RuntimeException("Not an application '" + exp + "'");
        }
        return first(exp);
    }
    
    private static SExpression operands(SExpression exp)
    {
        if (!isApplication(exp)) {
            throw new RuntimeException("Not an application '" + exp + "'");
        }
        return second(exp);
    }
    
    private static SExpression listOfValues(SExpression exp, SExpression env)
    {
        if (exp == null) {
            return null;
        } else {
            return new Pair(
                    eval(first(exp), env),
                    listOfValues(second(exp), env));
        }
    }
}
