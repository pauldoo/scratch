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
            const bool IsSelfEvaluating(const ObjectPtr& exp)
            {
                return exp->IsA<PrimitiveBase>();
            }
            
            const bool IsVariable(const ObjectPtr& exp)
            {
                return exp->IsA<Symbol>();
            }
            
            const bool IsPairWithFirstAsSymbolWithValue(const ObjectPtr& exp, const std::wstring& symbolName)
            {
                const boost::intrusive_ptr<Pair> pair00 = exp->AsA00<Pair>();
                if (pair00 != NULL) {
                    const boost::intrusive_ptr<Symbol> symbol00 = pair00->mFirst->AsA00<Symbol>();
                    if (symbol00 != NULL) {
                        return symbol00->mName == symbolName;
                    }
                }
                return false;
            }
            
            const bool IsLambda(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, LAMBDA);
            }

            const bool IsDefine(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, DEFINE);
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

            const ObjectPtr LookupVariableValueInEnvironment(const ObjectPtr& exp, const ObjectPtr& env)
            {
                BOOST_ASSERT(env != NULL /*, "unbound variable"*/);
            
                const ObjectPtr entry = LookupVariableEntryInFrame(exp, env->AsA<Pair>()->mFirst);
                if (entry != NULL) {
                    return entry->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
                } else {
                    return LookupVariableValueInEnvironment(exp, env->AsA<Pair>()->mSecond);
                }
            }
            
            const ObjectPtr MakeProcedure(const ObjectPtr& parameters, const ObjectPtr& body, const ObjectPtr& env)
            {
                return
                    Pair::New(Symbol::New(COMPOUND_PROCEDURE),
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
            
            const ObjectPtr LambdaBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsLambda(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
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
            
            const bool IsCompoundProcedure(const ObjectPtr& exp)
            {
                return IsPairWithFirstAsSymbolWithValue(exp, COMPOUND_PROCEDURE);
            }
            
            const ObjectPtr ProcedureParameters(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsCompoundProcedure(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr ProcedureBody(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsCompoundProcedure(exp));
                return exp->AsA<Pair>()->mSecond->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
            }
            
            const ObjectPtr ProcedureEnvironment(const ObjectPtr& exp)
            {
                BOOST_ASSERT(IsCompoundProcedure(exp));
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
                if (parameterNames == NULL && parameterValues == NULL) {
                    return baseEnv;
                }
                BOOST_ASSERT(parameterNames != NULL /*, "Too many arguments"*/);
                BOOST_ASSERT(parameterValues != NULL /*, "Too few arguments"*/);
                
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
            
            void DefineVariable(const ObjectPtr& name, const ObjectPtr& value, const ObjectPtr& env)
            {
                env->AsA<Pair>()->mFirst =
                        ExtendFrame(
                            Pair::New(name, ObjectPtr()),
                            Pair::New(value, ObjectPtr()),
                            env->AsA<Pair>()->mFirst);
            }
        }
        
        const std::wstring LAMBDA = L"lambda";
        const std::wstring DEFINE = L"define";
        const std::wstring COMPOUND_PROCEDURE = L"compound-procedure";
        
        const ObjectPtr Eval(const ObjectPtr& exp, const ObjectPtr& env)
        {
            if (IsSelfEvaluating(exp)) {
                return exp;
            } else if (IsVariable(exp)) {
                return LookupVariableValueInEnvironment(exp, env);
            } else if (IsLambda(exp)) {
                return
                    MakeProcedure(LambdaParameters(exp), LambdaBody(exp), env);
            } else if (IsDefine(exp)) {
                DefineVariable(
                        DefineName(exp),
                        Eval(DefineExpression(exp), env),
                        env);
                return ObjectPtr();
            } else if (IsApplication(exp)) {
                return
                    Apply(
                        Eval(Operator(exp), env),
                        ListOfValues(Operands(exp), env));
            } else {
                BOOST_ASSERT(false);
            }        
        }
        
        const ObjectPtr Apply(const ObjectPtr& procedure, const ObjectPtr& arguments)
        {
            if (IsCompoundProcedure(procedure)) {
                return Eval(
                        ProcedureBody(procedure),
                        ExtendEnvironment(
                            ProcedureParameters(procedure),
                            arguments,
                            ProcedureEnvironment(procedure)));
            } else {
                BOOST_ASSERT(false /*, "Not a compound procedure"*/);
            }
        }
    }
}
