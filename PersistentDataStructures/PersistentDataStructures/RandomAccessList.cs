using System;
namespace PersistentDataStructures
{
    /**
        Persistent list structure with O(1) insertion/removal at front.
        O(log(i)) access/update to i-th element.
        (Worst case bounds, no amortisation required.)
      
        This is a skew binary random access list.
    */
    public sealed class RandomAccessList<T>
    {
        private readonly int m_size;
        private readonly SinglyLinkedList<CompleteBinaryTree<T>> m_trees;

        private RandomAccessList(int size, SinglyLinkedList<CompleteBinaryTree<T>> trees)
        {
            m_size = size;
            m_trees = trees;
        }

        public static RandomAccessList<T> CreateEmpty()
        {
            return new RandomAccessList<T>(0, null);
        }

        public static RandomAccessList<T> PushFront(T value, RandomAccessList<T> list)
        {
            if (SinglyLinkedList<CompleteBinaryTree<T>>.Length(list.m_trees) >= 2)
            {
                CompleteBinaryTree<T> t1 = list.m_trees.m_head;
                CompleteBinaryTree<T> t2 = list.m_trees.m_tail.m_head;

                if (t1.Size == t2.Size)
                {
                    return new RandomAccessList<T>(
                        list.m_size + 1,
                        SinglyLinkedList<CompleteBinaryTree<T>>.PushFront(
                            CompleteBinaryTree<T>.Concatenate(
                                value,
                                t1,
                                t2),
                            list.m_trees.m_tail.m_tail));
                }
            }
            return new RandomAccessList<T>(
                list.m_size + 1,
                SinglyLinkedList<CompleteBinaryTree<T>>.PushFront(
                    CompleteBinaryTree<T>.CreateSingleElementTree(value),
                    list.m_trees));
        }

        public static RandomAccessList<T> PopFront(RandomAccessList<T> list)
        {
            if (list.m_trees == null) {
                throw new InvalidOperationException("List is empty.");
            }
            CompleteBinaryTree<T> t1;
            CompleteBinaryTree<T> t2;
            CompleteBinaryTree<T>.Split(list.m_trees.m_head, out t1, out t2);
            if (t1 != null) {
                return new RandomAccessList<T>(
                    list.m_size - 1,
                    SinglyLinkedList<CompleteBinaryTree<T>>.PushFront(
                        t1,
                        SinglyLinkedList<CompleteBinaryTree<T>>.PushFront(
                            t2,
                            list.m_trees.m_tail)));
            } else {
                return new RandomAccessList<T>(list.m_size - 1, list.m_trees.m_tail);
            }
        }

        public static T GetValue(RandomAccessList<T> list, int i)
        {
            return GetValue(list.m_trees, i);
        }

        private static T GetValue(SinglyLinkedList<CompleteBinaryTree<T>> trees, int i)
        {
            if (trees == null) {
                throw new IndexOutOfRangeException();
            }
            if (0 <= i && i < trees.m_head.Size)
            {
                return trees.m_head.GetIndex(i);
            }
            else
            {
                return GetValue(trees.m_tail, i - trees.m_head.Size);
            }
        }

        public static RandomAccessList<T> SetValue(RandomAccessList<T> list, int i, T value)
        {
            return new RandomAccessList<T>(list.m_size, SetValue(list.m_trees, i, value));
        }

        private static SinglyLinkedList<CompleteBinaryTree<T>> SetValue(
            SinglyLinkedList<CompleteBinaryTree<T>> trees, 
            int i, 
            T value)
        {
            if (trees == null)
            {
                throw new IndexOutOfRangeException();
            }
            if (0 <= i && i < trees.m_head.Size)
            {
                return SinglyLinkedList<CompleteBinaryTree<T>>.PushFront(
                    CompleteBinaryTree<T>.SetIndex(trees.m_head, i, value),
                    trees.m_tail);
            }
            else
            {
                return SinglyLinkedList<CompleteBinaryTree<T>>.PushFront(
                    trees.m_head,
                    SetValue(trees.m_tail, i - trees.m_head.Size, value));
            }
        }

        public int Size
        {
            get
            {
                return m_size;
            }
        }
    }
}
