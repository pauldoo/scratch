using NUnit.Framework;
using PersistentDataStructures;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;

namespace PersistentDataStructuresTest
{
    [TestFixture]
    class FingerTreeTest
    {
        [Test]
        public void Foobar()
        {
            FingerTree<int, int> tree = FingerTree<int, int>.CreateEmpty(new CountingMonoid<int>());
            for (int i = 1; i <= 100; i++)
            {
                tree = tree.PushBack(i);
                System.Diagnostics.Trace.WriteLine(tree.MonoidValue());
            }
        }

        [Test]
        public void Foobar2()
        {
            IList<FingerTree<int, int>> list_of_trees = new List<FingerTree<int, int>>();
            {
                FingerTree<int, int> tree = FingerTree<int, int>.CreateEmpty(new CountingMonoid<int>());
                for (int i = 0; i <= 100; i++)
                {
                    list_of_trees.Add(tree);
                    tree = tree.PushBack(i);
                }
            }

            for (int i = 0; i < list_of_trees.Count; i++)
            {
                for (int j = 0; j < list_of_trees.Count; j++)
                {
                    var expected = new List<int>();
                    for (int t = 0; t < i; t++)
                    {
                        expected.Add(t);
                    }
                    for (int t = 0; t < j; t++)
                    {
                        expected.Add(t);
                    }

                    var actual = list_of_trees[i].Append(list_of_trees[j]);
                    Assert.True(Enumerable.SequenceEqual(actual, expected));
                }
            }
        }
    }
}
