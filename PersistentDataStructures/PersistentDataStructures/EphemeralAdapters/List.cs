using System.Collections.Generic;
using System;
namespace PersistentDataStructures.EphemeralAdapters
{
    sealed public class List<T> : IList<T>, ICloneable
    {
        private FingerTree<T, int> m_finger_tree;

        public List()
        {
            Clear();
        }

        public int IndexOf(T item)
        {
            throw new NotImplementedException();
        }

        public void Insert(int index, T item)
        {
            var split = m_finger_tree.Split((int n) => (index < n));
            m_finger_tree = split.m_first.Force().PushBack(item).Append(split.m_second.Force());
        }

        public void RemoveAt(int index)
        {
            var split = m_finger_tree.SplitTree((int n) => (index < n), 0);
            m_finger_tree = split.m_left.Force().Append(split.m_right.Force());
        }

        public T this[int index]
        {
            get
            {
                var split = m_finger_tree.SplitTree((int n) => (index < n), 0);
                return split.m_value;
            }
            set
            {
                var split = m_finger_tree.SplitTree((int n) => (index < n), 0);
                m_finger_tree = split.m_left.Force().PushBack(value).Append(split.m_right.Force());
            }
        }

        public void Add(T item)
        {
            m_finger_tree = m_finger_tree.PushBack(item);
        }

        public void Clear()
        {
            m_finger_tree = new FingerTreeEmpty<T, int>(new CountingMonoid<T>());
        }

        public bool Contains(T item)
        {
            throw new NotImplementedException();
        }

        public void CopyTo(T[] array, int arrayIndex)
        {
            throw new NotImplementedException();
        }

        public int Count
        {
            get { return m_finger_tree.MonoidValue(); }
        }

        public bool IsReadOnly
        {
            get { return false; }
        }

        public bool Remove(T item)
        {
            throw new NotImplementedException();
        }

        public IEnumerator<T> GetEnumerator()
        {
            foreach (T val in m_finger_tree)
            {
                yield return val;
            }
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            foreach (T val in m_finger_tree)
            {
                yield return val;
            }
        }

        /// <summary>
        ///  For Cloning only.
        /// </summary>
        /// <param name="finger_tree"></param>
        private List(FingerTree<T, int> finger_tree)
        {
            m_finger_tree = finger_tree;
        }

        public object Clone()
        {
            return new List<T>(m_finger_tree);
        }

        public List<T> Concatenate(List<T> other)
        {
            return new List<T>(m_finger_tree.Append(other.m_finger_tree));
        }
    }

}
