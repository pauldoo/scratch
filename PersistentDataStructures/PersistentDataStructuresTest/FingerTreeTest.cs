using NUnit.Framework;
using PersistentDataStructures;

namespace PersistentDataStructuresTest
{
    [TestFixture]
    class FingerTreeTest
    {
        [Test]
        public void Foobar()
        {
            FingerTree<int, int> tree = new FingerTreeEmpty<int, int>(new CountingMonoid<int>());
            for (int i = 1; i <= 100; i++)
            {
                tree = tree.PushBack(i);
                System.Diagnostics.Trace.WriteLine(tree.MonoidValue());
            }
        }
    }
}
