delegate T S<T>(S<T> self);

Func<Func<Func<object[], object>, Func<object[], object>>, Func<object[], object>> Y = (F) => {
    S<Func<object[], object>> G = (K) => { return F((object[] a) => K(K)(a)); };
    return G(G);
};

Func<object[], object> factorial = Y((f) =>
  (object[] a) =>
      {
          int n = (int) a[0];
          if (n <= 0)
              return 1;
          else
              return n * (int)f(new object[] { n - 1 });
      });

Func<object[], object> fib = Y((f) =>
    (object[] a) =>
    {
        int n = (int) a[0];
        if (n <= 1)
            return n;
        else
            return ((int)f(new object[]{n - 1})) + ((int)f(new object[]{n - 2}));
    });

System.Diagnostics.Trace.WriteLine(factorial(new object[]{5}));
System.Diagnostics.Trace.WriteLine(fib(new object[] { 10 }));

