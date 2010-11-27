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

        private static double Benchmark(Action action)
        {
            double? result = null;
            for (int i = 1; i <= 1; i++) {
                Stopwatch stopwatch = new Stopwatch();
                stopwatch.Start();
                action();
                stopwatch.Stop();

                double this_time = stopwatch.ElapsedTicks / (double)Stopwatch.Frequency;
                if (result.HasValue == false || result.Value > this_time) {
                    result = this_time;
                }
            }
            return result.Value;
        }

        private static void TestPushBack<T>()
            where T : IList<int>, new()
        {
            T list = new T();

            for (int i = 1000000; i < 2000000; i++) {
                list.Add(i);
            }
        }

        private static void TestInsert<T>()
            where T : IList<int>, new()
        {
            T list = new T();

            for (int i = 0; i < 100; i++)
            {
                list.Add(i);
            }

            for (int i = 100; i < 1000000; i++)
            {
                list.Insert(i / 2, i);
            }
        }


        [Test]
        public void ListBenchmark()
        {
            IList<int> list = new PersistentDataStructures.EphemeralAdapters.List<int>();
            list.Add(1);
            list.Add(2);
            list.Add(3);
            list.Insert(0, 0);
            list.Insert(2, 9);
            list.Insert(5, 8);

            foreach (var v in list)
            {
                Trace.WriteLine(v);
            }

            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(() => TestPushBack<List<int>>())));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(() => TestPushBack<PersistentDataStructures.EphemeralAdapters.List<int>>())));

            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(() => TestInsert<List<int>>())));

            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(() => TestInsert<PersistentDataStructures.EphemeralAdapters.List<int>>())));
        }
    }
}
