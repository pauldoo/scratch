namespace PersistentDataStructures
{
    sealed public class Pair<T, U>
    {
        public readonly T m_first;
        public readonly U m_second;

        public Pair(T first, U second)
        {
            m_first = first;
            m_second = second;
        }
    }
}
