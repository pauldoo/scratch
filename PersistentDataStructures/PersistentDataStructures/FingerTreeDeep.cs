using System;
using System.Collections.Generic;
namespace PersistentDataStructures
{
    sealed internal class FingerTreeDeep<T, U> : FingerTree<T, U>
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

        public override bool IsEmpty()
        {
            return false;
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
                                lhs.m_suffix.ToList(),
                                SinglyLinkedList<T>.Concatenate(
                                    middle,
                                    rhs.m_prefix.ToList()))),
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

        private static Split<SinglyLinkedList<T>, T> SplitList(
            Monoid<T, U> monoid,
            Func<U, bool> predicate,
            U offset,
            SinglyLinkedList<T> list)
        {
            if (SinglyLinkedList<T>.Length(list) == 1) {
                return new Split<SinglyLinkedList<T>, T>(
                    SinglyLinkedList<T>.CreateEmpty(),
                    list.m_head,
                    SinglyLinkedList<T>.CreateEmpty());
            }
            U vhead = monoid.Combine(offset, monoid.Measure(list.m_head));
            if (predicate(vhead))
            {
                return new Split<SinglyLinkedList<T>, T>(
                    SinglyLinkedList<T>.CreateEmpty(),
                    list.m_head,
                    list.m_tail);
            }
            else
            {
                var split_rest = SplitList(monoid, predicate, vhead, list.m_tail);
                return new Split<SinglyLinkedList<T>, T>(
                    SinglyLinkedList<T>.PushFront(list.m_head, split_rest.m_left),
                    split_rest.m_value,
                    split_rest.m_right);
            }
        }

        private static FingerTree<T, U> DeepLeft(
            SinglyLinkedList<T> prefix,
            Delay<FingerTree<Node<T, U>, U>> middle,
            Digits<T> suffix,
            Monoid<T, U> monoid)
        {

            if (SinglyLinkedList<T>.IsEmpty(prefix))
            {
                if (middle.Force().IsEmpty())
                {
                    return FingerTree<T, U>.CreateEmpty(monoid).PushBackAll(suffix.ToList());
                }
                else
                {
                    return new FingerTreeDeep<T, U>(
                        Digits<T>.FromList(middle.Force().Front().ToList()),
                        new Delay<FingerTree<Node<T, U>, U>>(() =>
                            middle.Force().PopFront()),
                        suffix,
                        monoid);
                }
            }
            else
            {
                return new FingerTreeDeep<T, U>(
                    Digits<T>.FromList(prefix),
                    middle,
                    suffix,
                    monoid);
            }
        }
        private static FingerTree<T, U> DeepRight(
            Digits<T> prefix,
            Delay<FingerTree<Node<T, U>, U>> middle,
            SinglyLinkedList<T> suffix,
            Monoid<T, U> monoid)
        {

            if (SinglyLinkedList<T>.IsEmpty(suffix))
            {
                if (middle.Force().IsEmpty())
                {
                    return FingerTree<T, U>.CreateEmpty(monoid).PushBackAll(prefix.ToList());
                }
                else
                {
                    return new FingerTreeDeep<T, U>(
                        prefix,
                        new Delay<FingerTree<Node<T, U>, U>>(() =>
                            middle.Force().PopBack()),
                        Digits<T>.FromList(middle.Force().Back().ToList()),
                        monoid);
                }
            }
            else
            {
                return new FingerTreeDeep<T, U>(
                    prefix,
                    middle,
                    Digits<T>.FromList(suffix),
                    monoid);
            }
        }

        public override Split<Delay<FingerTree<T, U>>, T> SplitTree(Func<U, bool> predicate, U offset)
        {
            U vprefix = m_monoid.Combine(
                offset,
                m_prefix.ApplyMonoid(m_monoid));

            if (predicate(vprefix))
            {
                var split = SplitList(m_monoid, predicate, offset, m_prefix.ToList());
                return new Split<Delay<FingerTree<T, U>>, T>(
                    new Delay<FingerTree<T, U>>(() =>
                        CreateEmpty(m_monoid).PushBackAll(split.m_left)),
                    split.m_value,
                    new Delay<FingerTree<T, U>>(() =>
                        DeepLeft(split.m_right, m_middle, m_suffix, m_monoid)));
            }

            U vmiddle = m_monoid.Combine(
                vprefix,
                m_middle.Force().MonoidValue());

            if (predicate(vmiddle))
            {
                Split<Delay<FingerTree<Node<T, U>, U>>, Node<T, U>> deep_split =
                    m_middle.Force().SplitTree(predicate, vprefix);
                Split<SinglyLinkedList<T>, T> split = SplitList(
                    m_monoid,
                    predicate,
                    m_monoid.Combine(vprefix, deep_split.m_left.Force().MonoidValue()),
                    deep_split.m_value.ToList());

                return new Split<Delay<FingerTree<T, U>>, T>(
                    new Delay<FingerTree<T, U>>(() =>
                        DeepRight(m_prefix, deep_split.m_left, split.m_left, m_monoid)),
                    split.m_value,
                    new Delay<FingerTree<T, U>>(() =>
                        DeepLeft(split.m_right, deep_split.m_right, m_suffix, m_monoid)));
            } else {
                var split = SplitList(m_monoid, predicate, vmiddle, m_suffix.ToList());
                return new Split<Delay<FingerTree<T, U>>, T>(
                    new Delay<FingerTree<T, U>>(() =>
                        DeepRight(m_prefix, m_middle, split.m_left, m_monoid)),
                    split.m_value,
                    new Delay<FingerTree<T, U>>(() =>
                        CreateEmpty(m_monoid).PushBackAll(split.m_right)));
            }
        }

        public override T Front()
        {
            return m_prefix.Front();
        }
        public override T Back()
        {
            return m_suffix.Back();
        }
        public override FingerTree<T, U> PopFront()
        {
            return DeepLeft(
                SinglyLinkedList<T>.PopFront(m_prefix.ToList()),
                m_middle,
                m_suffix,
                m_monoid);
        }
        public override FingerTree<T, U> PopBack()
        {
            return DeepRight(
                m_prefix,
                m_middle,
                SinglyLinkedList<T>.PopBack(m_suffix.ToList()),
                m_monoid);
        }    
    }
}