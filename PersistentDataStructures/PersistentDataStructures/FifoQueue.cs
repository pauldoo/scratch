
using System;

namespace PersistentDataStructures
{
    /**
        Persistent FIFO (first in first out) queue with all operations
        running in O(1) worst case (not amortisation required.)
    */
    public sealed class FifoQueue<T>
    {
        private readonly int m_length;
        private readonly SinglyLinkedList<T> m_head;
        private readonly SinglyLinkedList<T> m_tail;
        private readonly SinglyLinkedList<T> m_reverse_head_in;
        private readonly SinglyLinkedList<T> m_reverse_head_out;
        private readonly SinglyLinkedList<T> m_reverse_tail_in;
        private readonly SinglyLinkedList<T> m_reverse_tail_out;

        private FifoQueue(
            int length,
            SinglyLinkedList<T> head,
            SinglyLinkedList<T> tail,
            SinglyLinkedList<T> reverse_head_in,
            SinglyLinkedList<T> reverse_head_out,
            SinglyLinkedList<T> reverse_tail_in,
            SinglyLinkedList<T> reverse_tail_out)
        {
            m_length = length;
            m_head = head;
            m_tail = tail;
            m_reverse_head_in = reverse_head_in;
            m_reverse_head_out = reverse_head_out;
            m_reverse_tail_in = reverse_tail_in;
            m_reverse_tail_out = reverse_tail_out;
        }

        private void AssertSanity()
        {
            System.Diagnostics.Debug.WriteLine(String.Format("AssertSanity: H:{0} T:{1} HI:{2} HO:{3} TI:{4} TO:{5}",
                SinglyLinkedList<T>.Length(m_head),
                SinglyLinkedList<T>.Length(m_tail),
                SinglyLinkedList<T>.Length(m_reverse_head_in),
                SinglyLinkedList<T>.Length(m_reverse_head_out),
                SinglyLinkedList<T>.Length(m_reverse_tail_in),
                SinglyLinkedList<T>.Length(m_reverse_tail_out)));

            if (m_head == null && (m_tail != null || m_reverse_tail_in != null || m_reverse_tail_out != null)) {
                throw new InvalidOperationException("Head can only be null if all tail lists are null");
            }
            if (SinglyLinkedList<T>.Length(m_head) <
                SinglyLinkedList<T>.Length(m_tail)) {
                throw new InvalidOperationException("Head is shorter than the tail");
            }

            if (m_length > (
                SinglyLinkedList<T>.Length(m_head) +
                SinglyLinkedList<T>.Length(m_tail) +
                SinglyLinkedList<T>.Length(m_reverse_tail_in) +
                SinglyLinkedList<T>.Length(m_reverse_tail_out)))
            {
                throw new InvalidOperationException("Length mismatch");
            }
        }

        public static FifoQueue<T> CreateEmpty() {
            var result = new FifoQueue<T>(0, null, null, null, null, null, null);
            result.AssertSanity();
            return result;
        }

        private static FifoQueue<T> Fixup(FifoQueue<T> queue)
        {
            queue = BeginTailReverse(queue);

            for (int i = 0; i < 3; i++)
            {
                queue = StepReverseTail(queue);
            }

            for (int i = 0; i < 3; i++) {
                queue = StepReverseHead(queue);
            }

            for (int i = 0; i < 3; i++) {
                queue = StepNewHeadPrependification(queue);
            }

            queue = BeginTailReverse(queue);
            queue.AssertSanity();
            return queue;
        }

        private static FifoQueue<T> BeginTailReverse(FifoQueue<T> queue)
        {
            if (SinglyLinkedList<T>.Length(queue.m_head) <
                SinglyLinkedList<T>.Length(queue.m_tail))
            {
                if (queue.m_reverse_head_in != null ||
                    queue.m_reverse_head_out != null ||
                    queue.m_reverse_tail_in != null ||
                    queue.m_reverse_tail_out != null)
                {
                    throw new InvalidOperationException("Reverse buffers should be empty when beginning a new reverse!");
                }
                return new FifoQueue<T>(
                    queue.m_length,
                    queue.m_head,
                    null,
                    queue.m_head,
                    null,
                    queue.m_tail,
                    null);
            }
            else
            {
                return queue;
            }

        }

        private static FifoQueue<T> StepNewHeadPrependification(FifoQueue<T> queue)
        {
            if (queue.m_reverse_tail_out != null)
            {
                // We have something that we might consider stealing straight away.

                if (SinglyLinkedList<T>.Length(queue.m_tail) +
                    SinglyLinkedList<T>.Length(queue.m_reverse_tail_out) == queue.m_length)
                {
                    return new FifoQueue<T>(
                        queue.m_length,
                        queue.m_reverse_tail_out,
                        queue.m_tail,
                        null,
                        null,
                        null,
                        null);
                }
            }

            if (queue.m_reverse_head_in == null &&
                queue.m_reverse_tail_in == null &&
                queue.m_reverse_head_out != null)
            {
                // Reversing of head and tail has completed, now continue to prepend reversed head onto the reversed tail.
                return new FifoQueue<T>(
                    queue.m_length,
                    queue.m_head,
                    queue.m_tail,
                    queue.m_reverse_head_in,
                    queue.m_reverse_head_out.m_tail,
                    queue.m_reverse_tail_in,
                    SinglyLinkedList<T>.Prepend(queue.m_reverse_head_out.m_head, queue.m_reverse_tail_out));
            }

            return queue;
        }

        private static FifoQueue<T> StepReverseTail(FifoQueue<T> queue)
        {
            if (queue.m_reverse_tail_in != null)
            {
                return new FifoQueue<T>(
                    queue.m_length,
                    queue.m_head,
                    queue.m_tail,
                    queue.m_reverse_head_in,
                    queue.m_reverse_head_out,
                    queue.m_reverse_tail_in.m_tail,
                    SinglyLinkedList<T>.Prepend(
                        queue.m_reverse_tail_in.m_head,
                        queue.m_reverse_tail_out));
            } else {
                return queue;
            }
        }

        private static FifoQueue<T> StepReverseHead(FifoQueue<T> queue) {
            if (queue.m_reverse_head_in != null) {
                return new FifoQueue<T>(
                    queue.m_length,
                    queue.m_head,
                    queue.m_tail,
                    queue.m_reverse_head_in.m_tail,
                    SinglyLinkedList<T>.Prepend(
                        queue.m_reverse_head_in.m_head,
                        queue.m_reverse_head_out),
                    queue.m_reverse_tail_in,
                    queue.m_reverse_tail_out);
            } else {
                return queue;
            }
        }

        public static FifoQueue<T> Enqueue(FifoQueue<T> queue, T value)
        {
            return Fixup(new FifoQueue<T>(
                queue.m_length + 1,
                queue.m_head,
                SinglyLinkedList<T>.Prepend(value, queue.m_tail),
                queue.m_reverse_head_in,
                queue.m_reverse_head_out,
                queue.m_reverse_tail_in,
                queue.m_reverse_tail_out));
        }

        public static FifoQueue<T> Dequeue(FifoQueue<T> queue)
        {
            return Fixup(new FifoQueue<T>(
                queue.m_length - 1,
                queue.m_head.m_tail,
                queue.m_tail,
                queue.m_reverse_head_in,
                queue.m_reverse_head_out,
                queue.m_reverse_tail_in,
                queue.m_reverse_tail_out));
        }

        public static T Peek(FifoQueue<T> queue)
        {
            return queue.m_head.m_head;
        }

        public static int Length(FifoQueue<T> queue)
        {
            return queue.m_length;
        }
    }
}
