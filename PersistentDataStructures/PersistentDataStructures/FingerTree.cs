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

    abstract class Node<T, U>
    {
        public readonly U m_monoid_value;

        public Node(U monoid_value)
        {
            m_monoid_value = monoid_value;
        }
    }

    // Hmm..  Could digits and nodes be the same thing?
    abstract class Digits<T>
    {
        public abstract U ApplyMonoid<U>(Monoid<T, U> monoid);

        public abstract int Count();
        public abstract Digits<T> PushFront(T value);
        public abstract Digits<T> PushBack(T value);
        public abstract Digits<T> PopFront();
        public abstract Digits<T> PopBack();
        public abstract T Front();
        public abstract T Back();
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
    }


    sealed class Digits1<T> : Digits<T>
    {
        public readonly T m_a;

        public override U ApplyMonoid<U>(Monoid<T,U> monoid)
        {
            return monoid.Measure(m_a);
        }

        public Digits1(T a)
        {
            m_a = a;
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

        public Digits2(T a, T b) {
            m_a = a;
            m_b = b;
        }

        public override U ApplyMonoid<U>(Monoid<T,U> monoid)
        {
            return
                monoid.Combine(
                    monoid.Measure(m_a),
                    monoid.Measure(m_b));
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

        public override U ApplyMonoid<U>(Monoid<T,U> monoid)
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

        public override U ApplyMonoid<U>(Monoid<T,U> monoid)
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

        public Digits4(T a, T b, T c, T d)
        {
            m_a = a;
            m_b = b;
            m_c = c;
            m_d = d;
        }

        public override int Count()
        {
            return 4;
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

    public abstract class FingerTree<T, U>
    {
        public readonly Monoid<T, U> m_monoid;

        public FingerTree(Monoid<T, U> monoid)
        {
            this.m_monoid = monoid;
        }

        public abstract U MonoidValue();

        public abstract FingerTree<T, U> PushFront(T value);
        public abstract FingerTree<T, U> PushBack(T value);
    }

    public sealed class FingerTreeEmpty<T, U> : FingerTree<T, U>
    {
        public FingerTreeEmpty(Monoid<T, U> monoid) : base(monoid)
        {
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

    sealed class Delay<T>
    {
        private System.Func<T> m_lambda00;
        private T m_result;

        public Delay(System.Func<T> lambda)
        {
            if (lambda == null)
            {
                throw new System.NullReferenceException();
            }
            m_lambda00 = lambda;
            m_result = default(T);
        }

        public T Force()
        {
            if (m_lambda00 != null)
            {
                lock (this)
                {
                    if (m_lambda00 != null)
                    {
                        m_result = m_lambda00.Invoke();
                        m_lambda00 = null;
                    }
                }
            }

            return m_result;
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
