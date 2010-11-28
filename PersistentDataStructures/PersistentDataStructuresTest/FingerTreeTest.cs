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

        private static double Benchmark(Action<Stopwatch> action)
        {
            double? result = null;
            for (int i = 1; i <= 2; i++) {
                Stopwatch stopwatch = new Stopwatch();
                action(stopwatch);

                double this_time = stopwatch.ElapsedTicks / (double)Stopwatch.Frequency;
                if (result.HasValue == false || result.Value > this_time) {
                    result = this_time;
                }
            }
            return result.Value;
        }

        private static void TestPushBack<T>(Stopwatch sw)
            where T : IList<int>, new()
        {
            T list = new T();

            sw.Start();
            for (int i = 0; i < 10000000; i++) {
                list.Add(i);
            }
            sw.Stop();
        }

        private static void TestInsertRemove<T>(Stopwatch sw)
            where T : IList<int>, new()
        {
            T list = new T();
            int size = 10000000;
            for (int i = 0; i < size; i++)
            {
                list.Add(i);
            }

            sw.Start();
            for (int i = 0; i < 1000; i++)
            {
                list.Insert(size / 2, i);
                list.RemoveAt(size / 2);
            }
            sw.Stop();
        }

        private static void TestRandomAccess<T>(Stopwatch sw)
            where T : IList<int>, new()
        {
            T list = new T();
            int size = 10000000;
            for (int i = 0; i < size; i++)
            {
                list.Add(i);
            }

            var rng = new Random(42);
            sw.Start();
            for (int i = 0; i < 100000; i++)
            {
                int index = rng.Next(size);
                int t = list[index];
                if (t != index)
                {
                    throw new ApplicationException("Value was wrong.");
                }
            }
            sw.Stop();
        }

        private static void TestClone<T>(Stopwatch sw)
            where T : IList<int>, new()
        {
            T list = new T();
            int size = 10000000;
            for (int i = 0; i < size; i++)
            {
                list.Add(i);
            }

            sw.Start();
            for (int i = 0; i < 100; i++)
            {
                var cloneable00 = list as ICloneable;
                if (cloneable00 != null)
                {
                    IList<int> copy = (IList<int>)cloneable00.Clone();
                }
                else
                {
                    IList<int> copy = new List<int>(list);
                }
            }
            sw.Stop();
        }

        private static void TestScan<T>(Stopwatch sw)
            where T : IList<int>, new()
        {
            T list = new T();
            int size = 10000000;
            for (int i = 0; i < size; i++)
            {
                list.Add(i);
            }

            sw.Start();
            for (int i = 0; i < 3; i++)
            {
                Int64 tot = 0;
                foreach (int v in list)
                {
                    tot += v;
                }
                if (tot != ((((Int64)size) * (size - 1)) / 2))
                {
                    throw new ApplicationException("Sum failed.");
                }
            }
            sw.Stop();
        }

        private static void TestConcatenate<T>(Stopwatch sw)
            where T : IList<int>, new()
        {
            T list = new T();
            int size = 10000000;
            for (int i = 0; i < size; i++)
            {
                list.Add(i);
            }

            sw.Start();
            for (int i = 0; i < 3; i++)
            {
                var list_as_wrapper00 = list as PersistentDataStructures.EphemeralAdapters.List<int>;
                if (list_as_wrapper00 != null)
                {
                    IList<int> result = list_as_wrapper00.Concatenate(list_as_wrapper00);
                }
                else
                {
                    IList<int> result = list.Concat(list).ToList();
                }
            }
            sw.Stop();
        }


        [Test]
        /*
            ***** PersistentDataStructuresTest.FingerTreeTest.ListBenchmark
            Push back:
            List<int>: 0.434755584027446s
            2-3<int>: 5.90214013220822s
            Insert/remove:
            List<int>: 8.04382116403663s
            2-3<int>: 0.125512305749122s
            Linear scan:
            List<int>: 0.459449042329934s
            2-3<int>: 3.55234567474616s
            Random access:
            List<int>: 0.00694533972887612s
            2-3<int>: 3.57417850546496s
            Clone:
            List<int>: 14.2591094175652s
            2-3<int>: 3.54421878108114E-05s
            Concatenate:
            List<int>: 4.25454579285725s
            2-3<int>: 0.00045144486724021s
        */
        public void ListBenchmark()
        {
            Trace.WriteLine("Push back:");
            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(TestPushBack<List<int>>)));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(TestPushBack<PersistentDataStructures.EphemeralAdapters.List<int>>)));

            Trace.WriteLine("Insert/remove:");
            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(TestInsertRemove<List<int>>)));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(TestInsertRemove<PersistentDataStructures.EphemeralAdapters.List<int>>)));

            Trace.WriteLine("Linear scan:");
            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(TestScan<List<int>>)));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(TestScan<PersistentDataStructures.EphemeralAdapters.List<int>>)));


            Trace.WriteLine("Random access:");
            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(TestRandomAccess<List<int>>)));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(TestRandomAccess<PersistentDataStructures.EphemeralAdapters.List<int>>)));

            Trace.WriteLine("Clone:");
            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(TestClone<List<int>>)));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(TestClone<PersistentDataStructures.EphemeralAdapters.List<int>>)));


            Trace.WriteLine("Concatenate:");
            Trace.WriteLine(String.Format(
                "List<int>: {0}s",
                Benchmark(TestConcatenate<List<int>>)));
            Trace.WriteLine(String.Format(
                "2-3<int>: {0}s",
                Benchmark(TestConcatenate<PersistentDataStructures.EphemeralAdapters.List<int>>)));
        
        }
    }
}
