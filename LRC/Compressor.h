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

#ifndef LRC_Compressor
#define LRC_Compressor

#include "Types.h"

namespace LRC { class RollingChecksum; }

namespace LRC
{
    class Compressor
    {
        public:
            Compressor(std::ostream* const output, const unsigned int block_size);
            ~Compressor();

            void Compress(std::istream& input);

        private:
            typedef std::map<StrongHash, unsigned int> StrongMap;
            typedef std::map<WeakHash, StrongMap> WeakMap;
            enum eMode { eMode_Block, eMode_Raw };

            void WriteRaw(const Byte);

            void SnipRawBuffer(void);

            void FlushRaw(void);

            void WriteBlock(const unsigned int offset);

            std::ostream* const m_output;
            const unsigned int m_block_size;

            unsigned int m_bytes_read;
            unsigned int m_bytes_to_skip;
            eMode m_mode;
            std::auto_ptr<RollingChecksum> m_checker;
            std::auto_ptr<std::vector<Byte> > m_current_raw00;
	    std::auto_ptr<WeakMap> m_previous_blocks;
    };
}

#endif

