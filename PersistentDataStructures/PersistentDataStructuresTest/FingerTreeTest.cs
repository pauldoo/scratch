using NUnit.Framework;
using PersistentDataStructures;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System;

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

        private static int Clamp(int min, int x, int max)
        {
            return Math.Max(min, Math.Min(x, max));
        }


        [Test]
        public void Foobar3()
        {
            FingerTree<int, int> tree = FingerTree<int, int>.CreateEmpty(new CountingMonoid<int>());
            for (int i = 0; i <= 100; i++)
            {
                tree = tree.PushBack(i);
            }

            for (int i = -10; i <= 110; i++)
            {
                Split<Delay<FingerTree<int, int>>, int> split = tree.SplitTree(
                    (n) => (i < n), 0);

                Trace.WriteLine(String.Format(
                    "{0}: {1} {2} {3}", 
                    i, 
                    split.m_left.Force().MonoidValue(),
                    split.m_value,
                    split.m_right.Force().MonoidValue()));


                Assert.AreEqual(Clamp(0, i, 100), split.m_value);
                Assert.AreEqual(Clamp(0, i, 100), split.m_left.Force().MonoidValue());
                Assert.AreEqual(Clamp(0, (100 - i), 100), split.m_right.Force().MonoidValue());

                {
                    var expected = new List<int>();
                    for (int t = 0; t < Clamp(0, i, 100); t++)
                    {
                        expected.Add(t);
                    }
                    Assert.True(Enumerable.SequenceEqual(expected, split.m_left.Force()));
                }
                {
                    var expected = new List<int>();
                    for (int t = Clamp(0, i, 100) + 1; t <= 100; t++)
                    {
                        expected.Add(t);
                    }
                    Assert.True(Enumerable.SequenceEqual(expected, split.m_right.Force()));
                }
            }

        }
    }
}
