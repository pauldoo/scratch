/*
    Copyright (c) 2013 Paul Richards <paul.richards@gmail.com>
    
    Permission to use, copy, modify, and/or distribute this software for any
    purpose with or without fee is hereby granted, provided that the above
    copyright notice and this permission notice appear in all copies.
    
    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package upgma;

import static java.lang.System.identityHashCode;

public final class Pair<T> extends Dendogram<T> {
    public final Dendogram<T> childA;
    public final Dendogram<T> childB;
    public final double averageLinkDistanceBetweenChildren;

    public Pair(final Dendogram<T> childA, final Dendogram<T> childB, final double averageLinkDistanceBetweenChildren) {
        super(childA.size + childB.size);
        this.childA = childA;
        this.childB = childB;
        this.averageLinkDistanceBetweenChildren = averageLinkDistanceBetweenChildren;
    }

    @Override
    // Shallow equality (are children identical references?)
    public boolean equals(final Object obj) {
        @SuppressWarnings("unchecked")
        final Pair<T> other = (Pair<T>) obj;

        return (this == other) || //
                (this.childA == other.childA && this.childB == other.childB);
    }

    @Override
    // Shallow equality (are children identical references?)
    public int hashCode() {
        return identityHashCode(childA) + identityHashCode(childB) * 31;
    }

    @Override
    public String toString() {
        return "{" + childA.toString() + " -(" + averageLinkDistanceBetweenChildren + ")- " + childB.toString() + "}";
    }

    @Override
    public void preOrderVisit(final ClusterVisitor<T> visitor) {
        visitor.visitPair(this);
        childA.preOrderVisit(visitor);
        childB.preOrderVisit(visitor);
    }
}