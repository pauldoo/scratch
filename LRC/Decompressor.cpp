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
#include "Decompressor.h"

namespace LRC
{
    Decompressor::Decompressor(std::iostream* const output)
      : m_output(output)
    {
    }

    Decompressor::~Decompressor()
    {
    }

    void Decompressor::Decompress(std::istream& input)
    {
        while (true) {
            unsigned int raw_size;
            input.read(reinterpret_cast<char*>(&raw_size), sizeof(unsigned int));
            if (!input) {
                return;
            }
            {
		//std::cerr << "Raw: " << raw_size << std::endl;
                std::vector<char> buffer(raw_size);
                input.read(&(buffer.front()), raw_size);
                m_output->write(&(buffer.front()), raw_size);
		if (!(*m_output)) {
		    throw std::string("Write failed");
		}
            }

            while (true) {
                unsigned int repeat_offset;
                input.read(reinterpret_cast<char*>(&repeat_offset), sizeof(unsigned int));
                if (!input) {
                    return;
                }
                if (repeat_offset == static_cast<unsigned int>(-1)) {
                    break;
                }
                unsigned int repeat_size;
                input.read(reinterpret_cast<char*>(&repeat_size), sizeof(unsigned int));

		//std::cerr << "Block: " << repeat_offset << " : " << repeat_size << std::endl;

                const unsigned int head = m_output->tellg();
                m_output->seekg(repeat_offset);
                std::vector<char> buffer(repeat_size);
                m_output->read(&(buffer.front()), repeat_size);
                m_output->seekg(head);
                m_output->write(&(buffer.front()), repeat_size);
		if (!(*m_output)) {
		    throw std::string("Write failed");
		}
            }
        }
    }
}
