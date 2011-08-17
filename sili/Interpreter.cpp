#include "Interpreter.h"

#include "Object.h"
#include "ObjectImp.h"
#include "Pair.h"
#include "Primitive.h"
#include "Symbol.h"

#include <string>

namespace sili {
    namespace Interpreter {
        namespace {
            // TODO: refactor and remove duplication..
            
            const std::wstring NIL = L"nil";
            const std::wstring CONS = L"cons";
            const std::wstring CAR = L"car";
            const std::wstring CDR = L"cdr";
            const std::wstring LAMBDA = L"lambda";
            const std::wstring DEFINE = L"define";
            const std::wstring MACRO = L"macro";
            const std::wstring LAMBDA_PROCEDURE = L"lambda-procedure";
            const std::wstring MACRO_PROCEDURE = L"macro-procedure";
            const std::wstring SET = L"set!";            
            
            const bool IsSelfEvaluating(const ObjectPtr& exp)
            {
                return exp->IsA<PrimitiveBase>();
            }
            
            const bool IsVariable(const ObjectPtr& exp)
            {
                return exp->IsA<Symbol>();
            }
            
            const bool IsSymbolWithValue(const ObjectPtr& exp, const std::wstring& symbolName)
            {
                const boost::intrusive_ptr<Symbol> symbol00 = exp->AsA00<Symbol>();
                return
                        symbol00 != NULL &&
                        symbol00->mName == symbolName;
            }
            
            const bool IsPairWithFirstAsSymbolWithValue(const ObjectPtr& exp, const std::wstring& symbolName)
            {
                const boost::intrusive_ptr<Pair> pair00 = exp->AsA00<Pair>();
                return
                        pair00 != NULL &&
                        IsSymbolWithValue(pair00->mFirst, symbolName);
            }
            
            const bool IsLambda(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, LAMBDA);
            }

            const bool IsNil(const ObjectPtr& exp)
            {
                return IsSymbolWithValue(exp, NIL);
            }
            const bool IsCons(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, CONS);
            }
            const bool IsCar(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, CAR);
            }
            const bool IsCdr(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, CDR);
            }
            const bool IsMacro(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, MACRO);
            }

            const bool IsDefine(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, DEFINE);
            }

            const bool IsSet(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, SET);
            }
            
            const bool IsApplication(const ObjectPtr& exp)
            {
                return exp->IsA<Pair>();
            }
            
            const ObjectPtr LookupVariableEntryInFrame(const ObjectPtr& exp, const ObjectPtr& frame)
            {
                if (frame == NULL) {
                    return ObjectPtr();
                } else {
                    const std::wstring& nameToResolve = exp->AsA<Symbol>()->mName;
                    const ObjectPtr entry = frame->AsA<Pair>()->mFirst;
                    const std::wstring& nameInEnvironment = entry->AsA<Pair>()->mFirst->AsA<Symbol>()->mName;

                    if (nameInEnvironment == nameToResolve) {
                        return entry;
                    } else {
                        return LookupVariableEntryInFrame(exp, frame->AsA<Pair>()->mSecond);
                    }
                }
            }

            const ObjectPtr LookupVariableEntryInEnvironment(const ObjectPtr& exp, const ObjectPtr& env)
            {
                BOOST_ASSERT(env != NULL /*, "unbound variable"*/);
            
                const ObjectPtr entry = LookupVariableEntryInFrame(exp, env->AsA<Pair>()->mFirst);
                if (entry != NULL) {
                    return entry;
                } else {
                    return LookupVariableEntryInEnvironment(exp, env->AsA<Pair>()->mSecond);
                }
            }

            const ObjectPtr LookupVariableValueInEnvironment(const ObjectPtr& exp, const ObjectPtr& env)
            {
                const ObjectPtr entry = LookupVariableEntryInEnvironment(exp, env);
                return entry->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr MakeProcedure(const ObjectPtr& parameters, const ObjectPtr& body, const ObjectPtr& env)
            {
                return
                    Pair::New(Symbol::New(LAMBDA_PROCEDURE),
                        Pair::New(parameters,
                            Pair::New(body,
                                Pair::New(env,
                                    NULL))));
            }

            const ObjectPtr MakeMacro(const ObjectPtr& parameters, const ObjectPtr& body, const ObjectPtr& env)
            {
                return
                    Pair::New(Symbol::New(MACRO_PROCEDURE),
                        Pair::New(parameters,
                            Pair::New(body,
                                Pair::New(env,
                                    NULL))));
            }
            
            const ObjectPtr LambdaParameters(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambda(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }

            const ObjectPtr MacroParameters(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsMacro(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr LambdaBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambda(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond;
            }

            const ObjectPtr MacroBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsMacro(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond;
            }            
            
            const ObjectPtr Operator(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsApplication(exp));
                return exp->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr Operands(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsApplication(exp));
                return exp->AsA<Pair>()->mSecond;
            }
            
            const ObjectPtr ListOfValues(const ObjectPtr& exp, const ObjectPtr& env)
            {
                if (exp == NULL) {
                    return NULL;
                } else {
                    return Pair::New(
                            Eval(exp->AsA<Pair>()->mFirst, env),
                            ListOfValues(exp->AsA<Pair>()->mSecond, env));
                }
            }
            
            const bool IsLambdaProcedure(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, LAMBDA_PROCEDURE);
            }

            const bool IsMacroProcedure(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, MACRO_PROCEDURE);
            }
            
            const ObjectPtr ProcedureParameters(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambdaProcedure(exp) || IsMacroProcedure(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr ProcedureBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambdaProcedure(exp) || IsMacroProcedure(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr ProcedureEnvironment(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambdaProcedure(exp) || IsMacroProcedure(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr ExtendFrame(
                const ObjectPtr& variableNames,
                const ObjectPtr& variableValues,
                const ObjectPtr& baseFrame)
            {
                if (variableNames == NULL && variableValues == NULL) {
                    return baseFrame;
                }
                BOOST_ASSERT(variableNames != NULL /*, "Too many arguments"*/);
                BOOST_ASSERT(variableValues != NULL /*, "Too few arguments"*/);

                // TODO: assert that names don't clash with existing variables
                return
                    Pair::New(
                        Pair::New(
                            variableNames->AsA<Pair>()->mFirst->AsA<Symbol>(),
                            Pair::New(
                                variableValues->AsA<Pair>()->mFirst,
                                NULL)),
                        ExtendFrame(
                            variableNames->AsA<Pair>()->mSecond,
                            variableValues->AsA<Pair>()->mSecond,
                            baseFrame));
            }
                    
            
            const ObjectPtr ExtendEnvironment(
                const ObjectPtr& parameterNames, 
                const ObjectPtr& parameterValues, 
                const ObjectPtr& baseEnv)
            {
                return
                    Pair::New(
                        ExtendFrame(parameterNames, parameterValues, ObjectPtr()),
                        baseEnv);
            }
            
            const ObjectPtr DefineName(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsDefine(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr DefineExpression(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsDefine(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            void DefineVariableInEnvironment(const ObjectPtr& name, const ObjectPtr& value, const ObjectPtr& env)
            {
                env->AsA<Pair>()->mFirst =
                        ExtendFrame(
                            Pair::New(name, ObjectPtr()),
                            Pair::New(value, ObjectPtr()),
                            env->AsA<Pair>()->mFirst);
            }
            
            const ObjectPtr EvalExpressions(const ObjectPtr& exps, const ObjectPtr& env)
            {
                ObjectPtr result = Eval(exps->AsA<Pair>()->mFirst, env);
                if (exps->AsA<Pair>()->mSecond == NULL) {
                    return result;
                } else {
                    return EvalExpressions(exps->AsA<Pair>()->mSecond, env);
                }
            }
            
            const ObjectPtr SetName(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsSet(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }     
            
            const ObjectPtr SetExpression(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsSet(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr SetVariableInEnvironment(const ObjectPtr& name, const ObjectPtr& value, const ObjectPtr& env)
            {
                const ObjectPtr entry = LookupVariableEntryInEnvironment(name, env);
                entry->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst = value;
                return value;
            }
        }
        
        const ObjectPtr Eval(const ObjectPtr& exp, const ObjectPtr& env)
        {
            if (IsNil(exp)) {
                return ObjectPtr();
            } else if (IsSelfEvaluating(exp)) {
                return exp;
            } else if (IsVariable(exp)) {
                return LookupVariableValueInEnvironment(exp, env);
            } else if (IsLambda(exp)) {
                return
                    MakeProcedure(LambdaParameters(exp), LambdaBody(exp), env);
            } else if (IsMacro(exp)) {
                return
                    MakeMacro(MacroParameters(exp), MacroBody(exp), env);
            } else if (IsDefine(exp)) {
                DefineVariableInEnvironment(
                        DefineName(exp),
                        Eval(DefineExpression(exp), env),
                        env);
                return ObjectPtr();
            } else if (IsSet(exp)) {
                return SetVariableInEnvironment(
                        SetName(exp),
                        Eval(SetExpression(exp), env),
                        env);
            } else if (IsCons(exp)) {
                return Pair::New(
                        Eval(exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst, env),
                        Eval(exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst, env));
            } else if (IsCar(exp)) {
                return Eval(exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst, env)->AsA<Pair>()->mFirst;
            } else if (IsCdr(exp)) {
                return Eval(exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst, env)->AsA<Pair>()->mSecond;
            } else if (IsApplication(exp)) {
                return
                    Apply(
                        Eval(Operator(exp), env),
                        Operands(exp),
                        env);
            } else {
                BOOST_ASSERT(false);
            }        
        }
        
        const ObjectPtr Apply(const ObjectPtr& procedure, const ObjectPtr& argumentExpressions, const ObjectPtr& environment)
        {
            if (IsLambdaProcedure(procedure)) {
                return EvalExpressions(
                        ProcedureBody(procedure),
                        ExtendEnvironment(
                            ProcedureParameters(procedure),
                            ListOfValues(argumentExpressions, environment),
                            ProcedureEnvironment(procedure)));
            } else if (IsMacroProcedure(procedure)) {
                return Eval(
                    Eval(
                        ProcedureBody(procedure)->AsA<Pair>()->mFirst,
                        ExtendEnvironment(
                            ProcedureParameters(procedure),
                            argumentExpressions,
                            ProcedureEnvironment(procedure))),
                    environment);
            } else {
                BOOST_ASSERT(false /*, "Not a compound procedure"*/);
            }
        }
    }
}
