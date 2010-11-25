using System;
using System.Collections.Generic;
namespace PersistentDataStructures
{
    sealed internal class FingerTreeSingle<T, U> : FingerTree<T, U>
    {
        public readonly T m_value;

        public FingerTreeSingle(T value, Monoid<T, U> monoid)
            : base(monoid)
        {
            m_value = value;
        }

        public override bool IsEmpty()
        {
            return false;    
        }

        public override FingerTree<T, U> Append(FingerTree<T, U> rhs)
        {
            return rhs.PushFront(m_value);
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield return m_value;
        }

        internal override FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs)
        {
            return lhs.PushBack(m_value);
        }

        internal override FingerTree<T, U> Append(SinglyLinkedList<T> middle, FingerTree<T, U> rhs)
        {
            return rhs.PushFrontAll(middle).PushFront(m_value);
        }

        internal override FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs, SinglyLinkedList<T> middle)
        {
            return lhs.PushBackAll(middle).PushBack(m_value);
        }

        public override U MonoidValue()
        {
            return m_monoid.Measure(m_value);
        }

        public override FingerTree<T, U> PushFront(T value)
        {
            return new FingerTreeDeep<T, U>(
                new Digits1<T>(value),
                new Delay<FingerTree<Node<T, U>, U>>(() =>
                    new FingerTreeEmpty<Node<T, U>, U>(new DeeperMonoid<T, U>(m_monoid))),
                new Digits1<T>(m_value),
                m_monoid);
        }

        public override FingerTree<T, U> PushBack(T value)
        {
            return new FingerTreeDeep<T, U>(
                new Digits1<T>(m_value),
                new Delay<FingerTree<Node<T, U>, U>>(() =>
                    new FingerTreeEmpty<Node<T, U>, U>(new DeeperMonoid<T, U>(m_monoid))),
                new Digits1<T>(value),
                m_monoid);
        }

        public override Split<Delay<FingerTree<T, U>>, T> SplitTree(Func<U, bool> predicate, U offset)
        {
            return new Split<Delay<FingerTree<T, U>>, T>(
                new Delay<FingerTree<T, U>>(() => new FingerTreeEmpty<T, U>(m_monoid)),
                m_value,
                new Delay<FingerTree<T, U>>(() => new FingerTreeEmpty<T, U>(m_monoid)));
        }

        public override T Front()
        {
            return m_value;
        }
        public override T Back()
        {
            return m_value;
        }
        public override FingerTree<T, U> PopFront()
        {
            return new FingerTreeEmpty<T, U>(m_monoid);
        }
        public override FingerTree<T, U> PopBack()
        {
            return new FingerTreeEmpty<T, U>(m_monoid);
        }    
    }
}