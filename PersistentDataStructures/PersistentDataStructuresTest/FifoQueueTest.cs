using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NUnit.Framework;
using PersistentDataStructures;

namespace PersistentDataStructuresTest
{
    [TestFixture]
    class FifoQueueTest
    {
        [Test]
        public void Foobar()
        {
            FifoQueue<int> queue = FifoQueue<int>.CreateEmpty();
            int size = 0;

            for (int i = 0; i < 1000; i++)
            {
                queue = FifoQueue<int>.Enqueue(queue, i);
                size++;
                Assert.AreEqual(size, FifoQueue<int>.Length(queue));
                Assert.AreEqual(0, FifoQueue<int>.Peek(queue));
            }

            for (int i = 1000; i < 2000; i++)
            {
                Assert.AreEqual(i - 1000, FifoQueue<int>.Peek(queue));
                queue = FifoQueue<int>.Enqueue(FifoQueue<int>.Dequeue(queue), i);
                Assert.AreEqual(size, FifoQueue<int>.Length(queue));
            }

            for (int i = 2000; i < 3000; i++)
            {
                Assert.AreEqual(i - 1000, FifoQueue<int>.Peek(queue));
                queue = FifoQueue<int>.Dequeue(queue);
                size--;
                Assert.AreEqual(size, FifoQueue<int>.Length(queue));
            }
        }
    }
}
