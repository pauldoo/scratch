namespace PersistentDataStructures
{
    public interface Monoid<T, U>
    {
        U Measure(T element);

        U Combine(U a, U b);

        U Identity();
    }

    sealed internal class DeeperMonoid<T, U> : Monoid<Node<T, U>, U>
    {
        public readonly Monoid<T, U> m_nested_monoid;

        public DeeperMonoid(Monoid<T, U> nested_monoid)
        {
            m_nested_monoid = nested_monoid;
        }

        public U Measure(Node<T, U> value)
        {
            return value.m_monoid_value;
        }

        public U Combine(U a, U b)
        {
            return m_nested_monoid.Combine(a, b);
        }

        public U Identity()
        {
            return m_nested_monoid.Identity();
        }
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