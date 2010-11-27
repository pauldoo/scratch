using System;
using System.Collections.Generic;
namespace PersistentDataStructures
{
    sealed internal class FingerTreeEmpty<T, U> : FingerTree<T, U>
    {
        public FingerTreeEmpty(Monoid<T, U> monoid)
            : base(monoid)
        {
        }

        public override bool IsEmpty()
        {
            return true;
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            yield break;
        }

        public override FingerTree<T, U> Append(FingerTree<T, U> rhs)
        {
            return rhs;
        }

        internal override FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs)
        {
            return lhs;
        }

        internal override FingerTree<T, U> Append(SinglyLinkedList<T> middle, FingerTree<T, U> rhs)
        {
            return rhs.PushFrontAll(middle);
        }

        internal override FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs, SinglyLinkedList<T> middle)
        {
            return lhs.PushBackAll(middle);
        }

        public override U MonoidValue()
        {
            return m_monoid.Identity();
        }

        public override FingerTree<T, U> PushFront(T value)
        {
            return new FingerTreeSingle<T, U>(value, m_monoid);
        }

        public override FingerTree<T, U> PushBack(T value)
        {
            return PushFront(value);
        }

        public override Split<Delay<FingerTree<T, U>>, T> SplitTree(Func<U, bool> predicate, U offset)
        {
            throw new InvalidOperationException("Colleciton is empty.");
        }

        public override T Front()
        {
            throw new InvalidOperationException("Colleciton is empty.");
        }
        public override T Back()
        {
            throw new InvalidOperationException("Colleciton is empty.");
        }
        public override FingerTree<T, U> PopFront()
        {
            throw new InvalidOperationException("Colleciton is empty.");
        }
        public override FingerTree<T, U> PopBack()
        {
            throw new InvalidOperationException("Colleciton is empty.");
        }

        public override Pair<Delay<FingerTree<T, U>>, Delay<FingerTree<T, U>>> Split(Func<U, bool> predicate)
        {
            return new Pair<Delay<FingerTree<T, U>>, Delay<FingerTree<T, U>>>(
                new Delay<FingerTree<T, U>>(this),
                new Delay<FingerTree<T, U>>(this));
        }
    }
}