using System;
namespace PersistentDataStructures
{
    /**
        Perfectly balanced binary tree which stores elements
        at the internal nodes as well as at the leaves.
        Capable of storing exactly "2^N - 1" elements only.
    */
    sealed class CompleteBinaryTree<T>
    {
        private readonly int m_rank;
        private readonly T m_value;
        private readonly CompleteBinaryTree<T> m_left;
        private readonly CompleteBinaryTree<T> m_right;

        private CompleteBinaryTree(
            T value,
            CompleteBinaryTree<T> left,
            CompleteBinaryTree<T> right)
        {
            if ((left == null) != (right == null)) {
                throw new ArgumentException("Child trees must be of equal null-ness.");
            }
            int rank_of_left = (left == null) ? (-1) : (left.m_rank);
            int rank_of_right = (right == null) ? (-1) : (right.m_rank);
            if (rank_of_left != rank_of_right) {
                throw new ArgumentException("Child trees must be of equal rank.");
            }
            m_rank = rank_of_left + 1;
            m_value = value;
            m_left = left;
            m_right = right;

            if ((left != null && left.Size + right.Size + 1 != this.Size) ||
                (left == null && this.Size != 1))
            {
                throw new ArgumentException("New tree has wrong size somehow.");
            }
        }

        public static CompleteBinaryTree<T> CreateSingleElementTree(T value)
        {
            return new CompleteBinaryTree<T>(value, null, null);
        }

        /**
            Creates a new tree whose elements (when visited in pre-order traversal)
            will be the concatenation of the given value and the values from the two
            trees.
        */
        public static CompleteBinaryTree<T> Concatenate(
            T value,
            CompleteBinaryTree<T> tree1,
            CompleteBinaryTree<T> tree2)
        {
            return new CompleteBinaryTree<T>(value, tree1, tree2);
        }

        /**
            Return the two sub-trees.
        */
        public static void Split(
            CompleteBinaryTree<T> tree, 
            out CompleteBinaryTree<T> tree1,
            out CompleteBinaryTree<T> tree2)
        {
            tree1 = tree.m_left;
            tree2 = tree.m_right;
        }

        public int Size
        {
            get
            {
                return (1 << (m_rank + 1)) - 1;
            }
        }

        public T GetIndex(int i)
        {
            if (0 <= i && i < Size) {
                if (i == 0) {
                    return m_value;
                } else {
                    if ((i-1) < m_left.Size) {
                        return m_left.GetIndex(i - 1);
                    } else {
                        return m_right.GetIndex(i - 1 - m_left.Size);
                    }
                }
            } else {
                throw new IndexOutOfRangeException();
            }
        }

        public static CompleteBinaryTree<T> SetIndex(CompleteBinaryTree<T> tree, int i, T new_value)
        {
            if (0 <= i && i < tree.Size) {
                if (i == 0) {
                    return new CompleteBinaryTree<T>(new_value, tree.m_left, tree.m_right);
                } else {
                    if ((i-1) < tree.m_left.Size) {
                        return new CompleteBinaryTree<T>(
                            tree.m_value,
                            SetIndex(tree.m_left, i - 1, new_value),
                            tree.m_right);
                    } else {
                        return new CompleteBinaryTree<T>(
                            tree.m_value,
                            tree.m_left,
                            SetIndex(tree.m_right, i - 1 - tree.m_left.Size, new_value));
                    }
                }
            } else {
                throw new IndexOutOfRangeException();
            }
        }
    }
}
