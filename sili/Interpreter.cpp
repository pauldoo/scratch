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
            
            const std::wstring QUOTE = L"quote";
            const std::wstring CONS = L"cons";
            const std::wstring CAR = L"car";
            const std::wstring CDR = L"cdr";
            const std::wstring IF = L"if";
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
                const boost::intrusive_ptr<List> pair00 = exp->AsA00<List>();
                return
                        pair00 != NULL &&
                        IsSymbolWithValue(pair00->mHead, symbolName);
            }
            
            const bool IsLambda(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, LAMBDA);
            }

            const bool IsIf(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, IF);
            }
            
            const bool IsQuote(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, QUOTE);
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
                return exp->IsA<List>();
            }
            
            const ObjectPtr LookupVariableEntryInFrame(const ObjectPtr& exp, const ObjectPtr& frame)
            {
                if (frame == NULL) {
                    return ObjectPtr();
                } else {
                    const std::wstring& nameToResolve = exp->AsA<Symbol>()->mName;
                    const ObjectPtr entry = frame->AsA<List>()->mHead;
                    const std::wstring& nameInEnvironment = entry->AsA<List>()->mHead->AsA<Symbol>()->mName;

                    if (nameInEnvironment == nameToResolve) {
                        return entry;
                    } else {
                        return LookupVariableEntryInFrame(exp, frame->AsA<List>()->mTail);
                    }
                }
            }

            const ObjectPtr LookupVariableEntryInEnvironment(const ObjectPtr& exp, const ObjectPtr& env)
            {
                BOOST_ASSERT(env != NULL /*, "unbound variable"*/);
            
                const ObjectPtr entry = LookupVariableEntryInFrame(exp, env->AsA<List>()->mHead);
                if (entry != NULL) {
                    return entry;
                } else {
                    return LookupVariableEntryInEnvironment(exp, env->AsA<List>()->mTail);
                }
            }

            const ObjectPtr LookupVariableValueInEnvironment(const ObjectPtr& exp, const ObjectPtr& env)
            {
                const ObjectPtr entry = LookupVariableEntryInEnvironment(exp, env);
                return entry->AsA<List>()->mTail->mHead;
            }
            
            const ObjectPtr MakeProcedure(const ObjectPtr& parameters, const ObjectPtr& body, const ObjectPtr& env)
            {
                return
                    List::New(Symbol::New(LAMBDA_PROCEDURE),
                        List::New(parameters,
                            List::New(body,
                                List::New(env,
                                    NULL))));
            }

            const ObjectPtr MakeMacro(const ObjectPtr& parameters, const ObjectPtr& body, const ObjectPtr& env)
            {
                return
                    List::New(Symbol::New(MACRO_PROCEDURE),
                        List::New(parameters,
                            List::New(body,
                                List::New(env,
                                    NULL))));
            }
            
            const ObjectPtr LambdaParameters(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambda(exp));
                return exp->AsA<List>()->mTail->mHead;
            }

            const ObjectPtr MacroParameters(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsMacro(exp));
                return exp->AsA<List>()->mTail->mHead;
            }
            
            const ObjectPtr LambdaBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambda(exp));
                return exp->AsA<List>()->mTail->mTail;
            }

            const ObjectPtr MacroBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsMacro(exp));
                return exp->AsA<List>()->mTail->mTail;
            }            
            
            const ObjectPtr Operator(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsApplication(exp));
                return exp->AsA<List>()->mHead;
            }
            
            const ObjectPtr Operands(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsApplication(exp));
                return exp->AsA<List>()->mTail;
            }
            
            const ObjectPtr ListOfValues(const ObjectPtr& exp, const ObjectPtr& env)
            {
                if (exp == NULL) {
                    return NULL;
                } else {
                    return List::New(
                            Eval(exp->AsA<List>()->mHead, env),
                            ListOfValues(exp->AsA<List>()->mTail, env));
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
                return exp->AsA<List>()->mTail->mHead;
            }
            
            const ObjectPtr ProcedureBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambdaProcedure(exp) || IsMacroProcedure(exp));
                return exp->AsA<List>()->mTail->mTail->mHead;
            }
            
            const ObjectPtr ProcedureEnvironment(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambdaProcedure(exp) || IsMacroProcedure(exp));
                return exp->AsA<List>()->mTail->mTail->mTail->mHead;
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
                    List::New(
                        List::New(
                            variableNames->AsA<List>()->mHead->AsA<Symbol>(),
                            List::New(
                                variableValues->AsA<List>()->mHead,
                                NULL)),
                        ExtendFrame(
                            variableNames->AsA<List>()->mTail,
                            variableValues->AsA<List>()->mTail,
                            baseFrame));
            }
                    
            
            const ObjectPtr ExtendEnvironment(
                const ObjectPtr& parameterNames, 
                const ObjectPtr& parameterValues, 
                const ObjectPtr& baseEnv)
            {
                return
                    List::New(
                        ExtendFrame(parameterNames, parameterValues, ObjectPtr()),
                        baseEnv);
            }
            
            const ObjectPtr DefineName(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsDefine(exp));
                return exp->AsA<List>()->mTail->mHead;
            }
            
            const ObjectPtr DefineExpression(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsDefine(exp));
                return exp->AsA<List>()->mTail->mTail->mHead;
            }
            
            void DefineVariableInEnvironment(const ObjectPtr& name, const ObjectPtr& value, const ObjectPtr& env)
            {
                env->AsA<List>()->mHead =
                        ExtendFrame(
                            List::New(name, ObjectPtr()),
                            List::New(value, ObjectPtr()),
                            env->AsA<List>()->mHead);
            }
            
            const ObjectPtr EvalExpressions(const ObjectPtr& exps, const ObjectPtr& env)
            {
                ObjectPtr result = Eval(exps->AsA<List>()->mHead, env);
                if (exps->AsA<List>()->mTail == NULL) {
                    return result;
                } else {
                    return EvalExpressions(exps->AsA<List>()->mTail, env);
                }
            }
            
            const ObjectPtr SetName(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsSet(exp));
                return exp->AsA<List>()->mTail->mHead;
            }     
            
            const ObjectPtr SetExpression(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsSet(exp));
                return exp->AsA<List>()->mTail->mTail->mHead;
            }
            
            const ObjectPtr SetVariableInEnvironment(const ObjectPtr& name, const ObjectPtr& value, const ObjectPtr& env)
            {
                const ObjectPtr entry = LookupVariableEntryInEnvironment(name, env);
                entry->AsA<List>()->mTail->mHead = value;
                return value;
            }
        }
        
        const ObjectPtr Eval(const ObjectPtr& exp, const ObjectPtr& env)
        {
            if (IsQuote(exp)) {
                return exp->AsA<List>()->mTail->mHead;
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
                return List::New(
                        Eval(exp->AsA<List>()->mTail->mHead, env),
                        Eval(exp->AsA<List>()->mTail->mTail->mHead, env));
            } else if (IsCar(exp)) {
                return Eval(exp->AsA<List>()->mTail->mHead, env)->AsA<List>()->mHead;
            } else if (IsCdr(exp)) {
                return Eval(exp->AsA<List>()->mTail->mHead, env)->AsA<List>()->mTail;
            } else if (IsIf(exp)) {
                const ObjectPtr testValue = Eval(exp->AsA<List>()->mTail->mHead, env);
                if (testValue != NULL) {
                    return Eval(exp->AsA<List>()->mTail->mTail->mHead, env);
                } else {
                    return Eval(exp->AsA<List>()->mTail->mTail->mTail->mHead, env);
                }
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
                        ProcedureBody(procedure)->AsA<List>()->mHead,
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
