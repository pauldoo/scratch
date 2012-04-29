// Long Range Compressor (LRC)
// Copyright (c) 2006, 2012 Paul Richards <paul.richards@gmail.com>
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

#ifndef LRC_RollingChecksum
#define LRC_RollingChecksum

#include "Types.h"

namespace LRC
{

    class RollingChecksum
    {
        public:
            RollingChecksum(const unsigned int block_size);
            ~RollingChecksum();

            void NextByte(const Byte);

            const WeakHash WeakChecksum(void) const;

            const StrongHash StrongChecksum(void) const;

        private:
            UInt16 m_rolling_a;
            UInt16 m_rolling_b;
            unsigned int m_offset;
            std::vector<Byte> m_buffer;
    };
}

#endif

