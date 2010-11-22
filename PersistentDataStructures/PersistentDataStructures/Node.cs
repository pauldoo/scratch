using System.Collections;
using System.Collections.Generic;
namespace PersistentDataStructures
{
    abstract class Node<T, U> : IEnumerable<T>
    {
        public readonly U m_monoid_value;

        public Node(U monoid_value)
        {
            m_monoid_value = monoid_value;
        }

        protected abstract IEnumerator<T> GetEnumeratorImp();

        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return GetEnumeratorImp();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumeratorImp();
        }
    }

    sealed class Node2<T, U> : Node<T, U>
    {
        public readonly T m_a;
        public readonly T m_b;

        public Node2(T a, T b, Monoid<T, U> monoid) :
            base(
                monoid.Combine(
                    monoid.Measure(a),
                    monoid.Measure(b)))
        {
            m_a = a;
            m_b = b;
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield return m_a;
            yield return m_b;
        }
    }

    sealed class Node3<T, U> : Node<T, U>
    {
        public readonly T m_a;
        public readonly T m_b;
        public readonly T m_c;

        public Node3(T a, T b, T c, Monoid<T, U> monoid) :
            base(
                monoid.Combine(
                    monoid.Combine(
                        monoid.Measure(a),
                        monoid.Measure(b)),
                    monoid.Measure(c)))
        {
            m_a = a;
            m_b = b;
            m_c = c;
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield return m_a;
            yield return m_b;
            yield return m_c;
        }
    }
}