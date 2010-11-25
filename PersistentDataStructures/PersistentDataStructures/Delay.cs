namespace PersistentDataStructures
{
    public sealed class Delay<T>
    {
        private System.Func<T> m_lambda00;
        private bool m_result_is_ready;
        private T m_result;
        private readonly object m_lock00;

        public Delay(System.Func<T> lambda)
        {
            if (lambda == null)
            {
                throw new System.NullReferenceException();
            }
            m_lambda00 = lambda;
            m_result_is_ready = false;
            m_result = default(T);
            m_lock00 = new object();
        }

        public Delay(T result)
        {
            m_lambda00 = null;
            m_result_is_ready = true;
            m_result = result;
        }

        private static X Magic<X>(X arg1, X arg2) {
            return arg1;
        }

        public T Force()
        {
            if (m_result_is_ready == false)
            {
                lock (m_lock00)
                {
                    if (m_result_is_ready == false)
                    {
                        m_result = Magic(m_lambda00, m_lambda00 = null).Invoke();
                        System.Threading.Thread.MemoryBarrier();
                        m_result_is_ready = true;
                    }
                }
            }

            return m_result;
        }
    }
}