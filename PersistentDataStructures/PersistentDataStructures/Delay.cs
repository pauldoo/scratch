namespace PersistentDataStructures
{
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

        public Delay(T result)
        {
            m_lambda00 = null;
            m_result = result;
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
}