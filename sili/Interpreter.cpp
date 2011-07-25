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
            
            const bool IsApplication(const ObjectPtr& exp)
            {
                return exp->IsA<Pair>();
            }
            
            const ObjectPtr LookupVariableValue(const ObjectPtr& exp, const ObjectPtr& env)
            {
                BOOST_ASSERT_MSG(env != NULL, "unbound variable");
                
                const std::wstring& nameToResolve = exp->AsA<Symbol>()->mName;
                const std::wstring& nameInEnvironment = env->AsA<Pair>()->mFirst->AsA<Pair>()->mFirst->AsA<Symbol>()->mName;
                
                std::wcout << L"Looking up '" << nameToResolve << L"\n";
                std::wcout << L"Using environment: " << (*(env.get())) << L"\n";
                
                
                if (nameInEnvironment == nameToResolve) {
                    return env->AsA<Pair>()->mFirst->AsA<Pair>()->mSecond->AsA<Pair>()->mFirst;
                } else {
                    return LookupVariableValue(exp, env->AsA<Pair>()->mSecond);
                }
            }
            
            const ObjectPtr MakeProcedure(const ObjectPtr& parameters, const ObjectPtr& body, const ObjectPtr& env)
            {
                std::wcout << "MakeProcedure: " << (*(body.get())) << "\n";
                std::wcout << "  with Params: " << (*(parameters.get())) << "\n";
                std::wcout << "  with Env: " << (*(env.get())) << "\n";
                
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
            
            const ObjectPtr ExtendEnvironment(
                const ObjectPtr& parameterNames, 
                const ObjectPtr& parameterValues, 
                const ObjectPtr& baseEnv)
            {
                if (parameterNames == NULL && parameterValues == NULL) {
                    return baseEnv;
                }
                BOOST_ASSERT_MSG(parameterNames != NULL, "Too many arguments");
                BOOST_ASSERT_MSG(parameterValues != NULL, "Too few arguments");
                
                return
                    Pair::New(
                        Pair::New(
                            parameterNames->AsA<Pair>()->mFirst->AsA<Symbol>(),
                            Pair::New(
                                parameterValues->AsA<Pair>()->mFirst,
                                NULL)),
                        ExtendEnvironment(
                            parameterNames->AsA<Pair>()->mSecond,
                            parameterValues->AsA<Pair>()->mSecond,
                            baseEnv));
            }
        }
        
        const std::wstring LAMBDA = L"lambda";
        const std::wstring COMPOUND_PROCEDURE = L"compound-procedure";
        
        const ObjectPtr Eval(const ObjectPtr& exp, const ObjectPtr& env)
        {
            std::wcout << "Eval: " << (*(exp.get())) << "\n";
            std::wcout << "  in Env: " << (*(env.get())) << "\n";
            
            if (IsSelfEvaluating(exp)) {
                return exp;
            } else if (IsVariable(exp)) {
                return LookupVariableValue(exp, env);
            } else if (IsLambda(exp)) {
                return
                    MakeProcedure(LambdaParameters(exp), LambdaBody(exp), env);
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
            std::wcout << "Apply: " << (*(procedure.get())) << "\n";
            std::wcout << "  with Args: " << (*(arguments.get())) << "\n";

            if (IsCompoundProcedure(procedure)) {
                return Eval(
                        ProcedureBody(procedure),
                        ExtendEnvironment(
                            ProcedureParameters(procedure),
                            arguments,
                            ProcedureEnvironment(procedure)));
            } else {
                BOOST_ASSERT_MSG(false, "Not a compound procedure");
            }
        }
    }
}