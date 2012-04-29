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

#include "External.h"
#include "RollingChecksum.h"

namespace LRC
{
    RollingChecksum::RollingChecksum(const unsigned int block_size)
      : m_rolling_a(0),
	m_rolling_b(0),
	m_offset(0),
        m_buffer(block_size)
    {
    }

    RollingChecksum::~RollingChecksum()
    {
    }

    void RollingChecksum::NextByte(const Byte new_byte)
    {
        const Byte old_byte = m_buffer[m_offset];
        m_buffer[m_offset] = new_byte;
        m_offset = (m_offset + 1) % m_buffer.size();

        m_rolling_a = m_rolling_a - old_byte + new_byte;
        m_rolling_b = m_rolling_b - (m_buffer.size() * old_byte) + m_rolling_a;
    }

    const WeakHash RollingChecksum::WeakChecksum() const
    {
        return (static_cast<UInt32>(m_rolling_b) << 16) | m_rolling_a;
    }

    const StrongHash RollingChecksum::StrongChecksum(void) const
    {
        CryptoPP::MD4 hasher;
        hasher.Update(&(m_buffer[m_offset]), m_buffer.size() - m_offset);
        hasher.Update(&(m_buffer[0]), m_offset);
        StrongHash result(hasher.DigestSize());
        hasher.Final(&(result.front()));
        return result;
    }
}

