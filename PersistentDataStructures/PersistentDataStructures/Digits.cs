using System.Collections;
using System.Collections.Generic;
namespace PersistentDataStructures
{
    // Hmm..  Could digits and nodes be the same thing?
    abstract class Digits<T> : IEnumerable<T>
    {
        public abstract U ApplyMonoid<U>(Monoid<T, U> monoid);

        public abstract int Count();
        public abstract Digits<T> PushFront(T value);
        public abstract Digits<T> PushBack(T value);
        public abstract Digits<T> PopFront();
        public abstract Digits<T> PopBack();
        public abstract T Front();
        public abstract T Back();
        public abstract SinglyLinkedList<T> AsList();

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

    sealed class Digits1<T> : Digits<T>
    {
        public readonly T m_a;

        public override U ApplyMonoid<U>(Monoid<T, U> monoid)
        {
            return monoid.Measure(m_a);
        }

        public Digits1(T a)
        {
            m_a = a;
        }

        public override SinglyLinkedList<T> AsList()
        {
            return
                SinglyLinkedList<T>.CreateSingle(m_a);
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield return m_a;
        }

        public override int Count()
        {
            return 1;
        }

        public override T Back()
        {
            return m_a;
        }

        public override T Front()
        {
            return m_a;
        }

        public override Digits<T> PushBack(T value)
        {
            return new Digits2<T>(m_a, value);
        }

        public override Digits<T> PushFront(T value)
        {
            return new Digits2<T>(value, m_a);
        }

        public override Digits<T> PopBack()
        {
            throw new System.InvalidOperationException();
        }

        public override Digits<T> PopFront()
        {
            throw new System.InvalidOperationException();
        }
    }

    sealed class Digits2<T> : Digits<T>
    {
        public readonly T m_a;
        public readonly T m_b;

        public Digits2(T a, T b)
        {
            m_a = a;
            m_b = b;
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield return m_a;
            yield return m_b;
        }

        public override U ApplyMonoid<U>(Monoid<T, U> monoid)
        {
            return
                monoid.Combine(
                    monoid.Measure(m_a),
                    monoid.Measure(m_b));
        }

        public override SinglyLinkedList<T> AsList()
        {
            return
                SinglyLinkedList<T>.PushFront(m_a,
                    SinglyLinkedList<T>.CreateSingle(m_b));
        }

        public override int Count()
        {
            return 2;
        }

        public override T Back()
        {
            return m_b;
        }

        public override T Front()
        {
            return m_a;
        }

        public override Digits<T> PushBack(T value)
        {
            return new Digits3<T>(m_a, m_b, value);
        }

        public override Digits<T> PushFront(T value)
        {
            return new Digits3<T>(value, m_a, m_b);
        }

        public override Digits<T> PopBack()
        {
            return new Digits1<T>(m_a);
        }

        public override Digits<T> PopFront()
        {
            return new Digits1<T>(m_b);
        }
    }
    sealed class Digits3<T> : Digits<T>
    {
        public readonly T m_a;
        public readonly T m_b;
        public readonly T m_c;

        public override U ApplyMonoid<U>(Monoid<T, U> monoid)
        {
            return
                monoid.Combine(
                    monoid.Combine(
                        monoid.Measure(m_a),
                        monoid.Measure(m_b)),
                    monoid.Measure(m_c));
        }
        public Digits3(T a, T b, T c)
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

        public override SinglyLinkedList<T> AsList()
        {
            return
                SinglyLinkedList<T>.PushFront(m_a,
                    SinglyLinkedList<T>.PushFront(m_b,
                        SinglyLinkedList<T>.CreateSingle(m_c)));
        }

        public override int Count()
        {
            return 3;
        }

        public override T Back()
        {
            return m_c;
        }

        public override T Front()
        {
            return m_a;
        }

        public override Digits<T> PushBack(T value)
        {
            return new Digits4<T>(m_a, m_b, m_c, value);
        }

        public override Digits<T> PushFront(T value)
        {
            return new Digits4<T>(value, m_a, m_b, m_c);
        }

        public override Digits<T> PopBack()
        {
            return new Digits2<T>(m_a, m_b);
        }

        public override Digits<T> PopFront()
        {
            return new Digits2<T>(m_b, m_c);
        }

        public Node3<T, U> PromoteToNode3<U>(Monoid<T, U> monoid)
        {
            return new Node3<T, U>(m_a, m_b, m_c, monoid);
        }
    }
    sealed class Digits4<T> : Digits<T>
    {
        public readonly T m_a;
        public readonly T m_b;
        public readonly T m_c;
        public readonly T m_d;

        public override U ApplyMonoid<U>(Monoid<T, U> monoid)
        {
            return
                monoid.Combine(
                    monoid.Combine(
                        monoid.Measure(m_a),
                        monoid.Measure(m_b)),
                    monoid.Combine(
                        monoid.Measure(m_c),
                        monoid.Measure(m_d)));
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield return m_a;
            yield return m_b;
            yield return m_c;
            yield return m_d;
        }
        
        public Digits4(T a, T b, T c, T d)
        {
            m_a = a;
            m_b = b;
            m_c = c;
            m_d = d;
        }

        public override SinglyLinkedList<T> AsList()
        {
            return
                SinglyLinkedList<T>.PushFront(m_a,
                    SinglyLinkedList<T>.PushFront(m_b,
                        SinglyLinkedList<T>.PushFront(m_c,
                            SinglyLinkedList<T>.CreateSingle(m_d))));
        }

        public override int Count()
        {
            return 4;
        }

        public override T Back()
        {
            return m_d;
        }

        public override T Front()
        {
            return m_a;
        }

        public override Digits<T> PushBack(T value)
        {
            throw new System.InvalidOperationException();
        }

        public override Digits<T> PushFront(T value)
        {
            throw new System.InvalidOperationException();
        }

        public override Digits<T> PopBack()
        {
            return new Digits3<T>(m_a, m_b, m_c);
        }

        public override Digits<T> PopFront()
        {
            return new Digits3<T>(m_b, m_c, m_d);
        }
    }
}
