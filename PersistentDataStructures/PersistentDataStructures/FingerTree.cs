using System.Collections.Generic;
using System.Collections;

namespace PersistentDataStructures
{
    public abstract class FingerTree<T, U> : IEnumerable<T>
    {
        public readonly Monoid<T, U> m_monoid;

        protected FingerTree(Monoid<T, U> monoid)
        {
            this.m_monoid = monoid;
        }

        public abstract U MonoidValue();

        protected abstract IEnumerator<T> GetEnumeratorImp();

        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return GetEnumeratorImp();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumeratorImp();
        }
        
        public abstract FingerTree<T, U> PushFront(T value);
        public abstract FingerTree<T, U> PushBack(T value);
        
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

        public static FingerTree<T, U> CreateEmpty(Monoid<T, U> monoid)
        {
            return new FingerTreeEmpty<T, U>(monoid);
        }

        public abstract FingerTree<T, U> Append(FingerTree<T, U> rhs);
        internal abstract FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs);

        internal abstract FingerTree<T, U> Append(SinglyLinkedList<T> middle, FingerTree<T, U> rhs);
        internal abstract FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs, SinglyLinkedList<T> middle);
    }

    sealed class FingerTreeEmpty<T, U> : FingerTree<T, U>
    {
        public FingerTreeEmpty(Monoid<T, U> monoid) : base(monoid)
        {
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
    }

    sealed class DeeperMonoid<T, U> : Monoid<Node<T, U>, U>
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
    
    sealed class FingerTreeSingle<T, U> : FingerTree<T, U>
    {
        public readonly T m_value;

        public FingerTreeSingle(T value, Monoid<T, U> monoid) : base(monoid)
        {
            m_value = value;
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
    }

    sealed class FingerTreeDeep<T, U> : FingerTree<T, U>
    {
        public readonly Delay<U> m_monoid_value;
        public readonly Digits<T> m_prefix;
        public readonly Delay<FingerTree<Node<T, U>, U>> m_middle;
        public readonly Digits<T> m_suffix;

        public FingerTreeDeep(
            Digits<T> prefix,
            Delay<FingerTree<Node<T, U>, U>> middle,
            Digits<T> suffix,
            Monoid<T, U> monoid) : base(monoid)
        {
            m_prefix = prefix;
            m_middle = middle;
            m_suffix = suffix;
            m_monoid_value = new Delay<U>(() => 
                m_monoid.Combine(
                    m_monoid.Combine(
                        m_prefix.ApplyMonoid(m_monoid),
                        m_middle.Force().MonoidValue()),
                    m_suffix.ApplyMonoid(m_monoid)));
        }

        protected override IEnumerator<T> GetEnumeratorImp()
        {
            foreach (var value in m_prefix)
            {
                yield return value;
            }

            foreach (var node in m_middle.Force())
            {
                foreach (var value in node)
                {
                    yield return value;
                }
            }

            foreach (var value in m_suffix)
            {
                yield return value;
            }
        }

        public override FingerTree<T, U> Append(FingerTree<T, U> rhs)
        {
            return rhs.PrependDeep(this);
        }

        internal override FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs)
        {
            return Concatenate(lhs, null, this);
        }

        internal override FingerTree<T, U> Append(SinglyLinkedList<T> middle, FingerTree<T, U> rhs)
        {
            return rhs.PrependDeep(this, middle);
        }

        internal override FingerTree<T, U> PrependDeep(FingerTreeDeep<T, U> lhs, SinglyLinkedList<T> middle)
        {
            return Concatenate(lhs, middle, this);
        }

        private static FingerTree<T, U> Concatenate(
            FingerTreeDeep<T, U> lhs, 
            SinglyLinkedList<T> middle,
            FingerTreeDeep<T, U> rhs)
        {
            return new FingerTreeDeep<T, U>(
                lhs.m_prefix,
                new Delay<FingerTree<Node<T, U>, U>>(
                    lhs.m_middle.Force().Append(
                        Nodes(
                            lhs.m_monoid,
                            SinglyLinkedList<T>.Concatenate(
                                lhs.m_suffix.AsList(),
                                SinglyLinkedList<T>.Concatenate(
                                    middle,
                                    rhs.m_prefix.AsList()))),
                        rhs.m_middle.Force())),
                rhs.m_suffix,
                lhs.m_monoid);
        }

        private static SinglyLinkedList<Node<T, U>> Nodes(
            Monoid<T, U> monoid,
            SinglyLinkedList<T> nodes)
        {
            T first = nodes.m_head;
            T second = nodes.m_tail.m_head;
            SinglyLinkedList<T> rest = nodes.m_tail.m_tail;
            switch (SinglyLinkedList<T>.Length(rest)) {
                case 0:
                    return 
                        SinglyLinkedList<Node<T, U>>.CreateSingle(
                            new Node2<T, U>(first, second, monoid));
                case 1:
                    return 
                        SinglyLinkedList<Node<T, U>>.CreateSingle(
                            new Node3<T, U>(first, second, rest.m_head, monoid));
                case 2:
                    return
                        SinglyLinkedList<Node<T, U>>.PushFront(
                            new Node2<T, U>(first, second, monoid),
                            Nodes(monoid, rest));
                default:
                    return
                        SinglyLinkedList<Node<T, U>>.PushFront(
                            new Node3<T, U>(first, second, rest.m_head, monoid),
                            Nodes(monoid, rest.m_tail));
            }
        }

        public override U MonoidValue()
        {
            return m_monoid_value.Force();
        }

        public override FingerTree<T, U> PushFront(T value)
        {
            switch (m_prefix.Count())
            {
                case 1:
                case 2:
                case 3:
                    return new FingerTreeDeep<T, U>(
                        m_prefix.PushFront(value),
                        m_middle,
                        m_suffix,
                        m_monoid);

                case 4:
                    m_middle.Force(); // Invoked for side effects
                    return new FingerTreeDeep<T, U>(
                        new Digits2<T>(value, m_prefix.Front()),
                        new Delay<FingerTree<Node<T, U>, U>>(() =>
                            m_middle.Force().PushFront(
                                ((Digits3<T>)(m_prefix.PopFront())).PromoteToNode3(m_monoid))),
                        m_suffix,
                        m_monoid);

                default:
                    throw new System.InvalidOperationException();
            }
        }

        public override FingerTree<T, U> PushBack(T value)
        {
            switch (m_suffix.Count()) {
                case 1:
                case 2:
                case 3:
                    return new FingerTreeDeep<T, U>(
                        m_prefix,
                        m_middle,
                        m_suffix.PushBack(value),
                        m_monoid);
                case 4:
                    m_middle.Force(); // Invoked for side effects
                    return new FingerTreeDeep<T, U>(
                        m_prefix,
                        new Delay<FingerTree<Node<T, U>, U>>(() =>
                            m_middle.Force().PushBack(
                                ((Digits3<T>)(m_suffix.PopBack())).PromoteToNode3(m_monoid))),
                        new Digits2<T>(m_suffix.Back(), value),
                        m_monoid);
                default:
                    throw new System.InvalidOperationException();
            }
        }
    }
}
