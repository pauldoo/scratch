using System.Collections.Generic;
using System.Collections;
using System;

namespace PersistentDataStructures
{
    /**
        TODO:
        * Consider squashing Digit/Node/List.
        * Split return values should use delays?
    */
    public abstract class FingerTree<T, U> : IEnumerable<T>
    {
        public readonly Monoid<T, U> m_monoid;

        public static FingerTree<T, U> CreateEmpty(Monoid<T, U> monoid)
        {
            return new FingerTreeEmpty<T, U>(monoid);
        }

        public abstract bool IsEmpty();
        public abstract U MonoidValue();
        public abstract FingerTree<T, U> PushFront(T value);
        public abstract FingerTree<T, U> PushBack(T value);
        public abstract FingerTree<T, U> Append(FingerTree<T, U> rhs);
        public abstract T Front();
        public abstract T Back();
        public abstract FingerTree<T, U> PopFront();
        public abstract FingerTree<T, U> PopBack();
        public abstract Split<Delay<FingerTree<T, U>>, T> SplitTree(Func<U, bool> predicate, U offset);

        public FingerTree<T, U> PushBackAll(SinglyLinkedList<T> values)
        {
            if (values == null)
            {
                return this;
            }
            else
            {
                return PushBack(values.m_head).PushBackAll(values.m_tail);
            }
        }

        public FingerTree<T, U> PushFrontAll(SinglyLinkedList<T> values)
        {
            if (values == null)
            {
                return this;
            }
            else
            {
                return PushFrontAll(values.m_tail).PushFront(values.m_head);
            }
        }

        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return GetEnumeratorImp();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumeratorImp();
        }

        internal abstract FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs);
        internal abstract FingerTree<T, U> Append(SinglyLinkedList<T> middle, FingerTree<T, U> rhs);
        internal abstract FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs, SinglyLinkedList<T> middle);

        protected abstract IEnumerator<T> GetEnumeratorImp();

        protected internal FingerTree(Monoid<T, U> monoid)
        {
            this.m_monoid = monoid;
        }
    }
}
