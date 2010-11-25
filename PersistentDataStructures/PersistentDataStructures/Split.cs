namespace PersistentDataStructures
{
    public sealed class Split<T, V>
    {
        public readonly T m_left;
        public readonly V m_value;
        public readonly T m_right;

        public Split(T left, V value, T right)
        {
            m_left = left;
            m_value = value;
            m_right = right;
        }
    }
}
