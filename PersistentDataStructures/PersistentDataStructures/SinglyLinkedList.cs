
namespace PersistentDataStructures
{
    public sealed class SinglyLinkedList<T>
    {
        private readonly int m_length;
        public readonly T m_head;
        public readonly SinglyLinkedList<T> m_tail;

        private SinglyLinkedList(T head, SinglyLinkedList<T> tail)
        {
            m_length = Length(tail) + 1;
            m_head = head;
            m_tail = tail;
        }

        public static SinglyLinkedList<T> CreateEmpty()
        {
            return null;
        }

        public static SinglyLinkedList<T> CreateSingle(T value)
        {
            return PushFront(value, CreateEmpty());
        }

        public static SinglyLinkedList<T> PushFront(T head, SinglyLinkedList<T> tail)
        {
            return new SinglyLinkedList<T>(head, tail);
        }

        public static int Length(SinglyLinkedList<T> list)
        {
            return (list == null) ? 0 : (list.m_length);
        }

        // O(Length(head))
        public static SinglyLinkedList<T> Concatenate(SinglyLinkedList<T> head, SinglyLinkedList<T> tail)
        {
            if (head == null)
            {
                return tail;
            }
            else
            {
                return PushFront(head.m_head, Concatenate(head.m_tail, tail));
            }
        }
    }
}
