using NUnit.Framework;
using PersistentDataStructures;

namespace PersistentDataStructuresTest
{
    [TestFixture]
    class RandomAccessListTest
    {
        [Test]
        public void Foobar()
        {
            RandomAccessList<int> my_list = RandomAccessList<int>.CreateEmpty();

            for (int i = 0; i <= 500; i++)
            {
                Assert.AreEqual(my_list.Size, i);
                my_list = RandomAccessList<int>.PushFront(i * 3, my_list);
                Assert.AreEqual(RandomAccessList<int>.GetValue(my_list, 0), i * 3);
                Assert.AreEqual(my_list.Size, i + 1);
            }

            for (int i = 0; i <= 500; i++)
            {
                Assert.AreEqual(RandomAccessList<int>.GetValue(my_list, (500 - i)), i * 3);
                my_list = RandomAccessList<int>.SetValue(my_list, (500-i), i * 6);
                Assert.AreEqual(RandomAccessList<int>.GetValue(my_list, (500-i)), i * 6);
            }
            for (int i = 500; i >= 0; i--)
            {
                Assert.AreEqual(RandomAccessList<int>.GetValue(my_list, (500 - i)), i * 6);
                my_list = RandomAccessList<int>.SetValue(my_list, (500-i), i * 9);
                Assert.AreEqual(RandomAccessList<int>.GetValue(my_list, (500-i)), i * 9);
            }

            for (int i = 0; i <= 500; i++)
            {
                Assert.AreEqual(my_list.Size, 500 - i + 1);
                Assert.AreEqual(RandomAccessList<int>.GetValue(my_list, 0), (500 - i) * 9);
                my_list = RandomAccessList<int>.PopFront(my_list);
                Assert.AreEqual(my_list.Size, 500 - i);
            }
        }
    }
}
