/*
    Copyright (c) 2009, 2012 Paul Richards <paul.richards@gmail.com>

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

package algorithmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class DancingLinksTest {

    private static boolean[][] createEmptyDenseMatrix(int rows, int columns) {
        boolean[][] result = new boolean[rows][];
        for (int i = 0; i < rows; i++) {
            result[i] = new boolean[columns];
        }
        return result;
    }

    @Test
    public void testSolve() {
        {
            boolean[][] matrix = createEmptyDenseMatrix(1, 1);
            matrix[0][0] = true;

            Set<Set<Integer>> solutions = DancingLinks.solve(DancingLinks.constructFromDenseMatrix(matrix));
            assertTrue(solutions.size() == 1);
            Set<Integer> solution = solutions.iterator().next();
            assertTrue(solution.size() == 1);
            assertTrue(solution.iterator().next() == 0);
        }
        {
            boolean[][] matrix = createEmptyDenseMatrix(2, 1);
            matrix[0][0] = true;
            matrix[1][0] = true;

            Set<Set<Integer>> solutions = DancingLinks.solve(DancingLinks.constructFromDenseMatrix(matrix));
            assertTrue(solutions.size() == 2);
            List<Set<Integer>> solutionsAsList = new ArrayList<Set<Integer>>(solutions);
            assertTrue(solutionsAsList.get(0).size() == 1);
            assertTrue(solutionsAsList.get(0).iterator().next() == 1);
            assertTrue(solutionsAsList.get(1).size() == 1);
            assertTrue(solutionsAsList.get(1).iterator().next() == 0);
        }
        {
            boolean[][] matrix = createEmptyDenseMatrix(2, 2);
            matrix[0][0] = true;
            matrix[1][0] = true;

            Set<Set<Integer>> solutions = DancingLinks.solve(DancingLinks.constructFromDenseMatrix(matrix));
            assertTrue(solutions.size() == 0);
        }
        {
            boolean[][] matrix = createEmptyDenseMatrix(2, 2);
            matrix[0][0] = true;
            matrix[1][1] = true;

            Set<Set<Integer>> solutions = DancingLinks.solve(DancingLinks.constructFromDenseMatrix(matrix));
            assertTrue(solutions.size() == 1);
            assertTrue(solutions.iterator().next().size() == 2);
            assertTrue((new ArrayList<Integer>(solutions.iterator().next())).get(0) == 1);
            assertTrue((new ArrayList<Integer>(solutions.iterator().next())).get(1) == 0);
        }
        {
            boolean[][] matrix = createEmptyDenseMatrix(2, 3);
            matrix[0][0] = true;
            matrix[0][1] = true;
            matrix[1][1] = true;
            matrix[1][2] = true;

            Set<Set<Integer>> solutions = DancingLinks.solve(DancingLinks.constructFromDenseMatrix(matrix));
            assertTrue(solutions.size() == 0);
        }
    }
}