namespace PersistentDataStructures
{
    public interface Monoid<T, U>
    {
        U Measure(T element);

        U Combine(U a, U b);

        U Identity();
    }

    public sealed class CountingMonoid<T> : Monoid<T, int>
    {
        public int Measure(T o)
        {
            return 1;
        }

        public int Combine(int a, int b)
        {
            return a + b;
        }

        public int Identity()
        {
            return 0;
        }
    }

    public sealed class MaximumMonoid : Monoid<int, int>
    {
        public int Measure(int o)
        {
            return o;
        }

        public int Combine(int a, int b)
        {
            return System.Math.Max(a, b);
        }

        public int Identity()
        {
            return System.Int32.MinValue;
        }
    }
}