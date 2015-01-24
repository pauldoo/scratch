package magiccoins;

import java.util.Arrays;

final class FoundSolutionException extends Exception {

    private static final long serialVersionUID = 271882788626071945L;

}

public final class MagicCoins {

    private final int numberOfCoins;
    private final int[] assignments;

    public MagicCoins(final int numberOfCoins) {
        this.numberOfCoins = numberOfCoins;
        this.assignments = new int[twoToThe(numberOfCoins)];
        Arrays.fill(this.assignments, -1);
        this.assignments[0] = 0;
    }

    public static void main(final String[] args) {

        for (int numberOfCoins = 1; numberOfCoins <= 8; numberOfCoins *= 2) {

            System.out.println("Trying " + numberOfCoins + " coins");
            final long startTime = System.currentTimeMillis();
            final MagicCoins magicCoins = new MagicCoins(numberOfCoins);
            try {
                magicCoins.solve();
                System.out.println("No solution found");
            } catch (final FoundSolutionException e) {
                System.out.println("Solution found:");
                magicCoins.printSolution();
            }
            final long endTime = System.currentTimeMillis();
            System.out.println(String.format("Took %dms", (endTime - startTime)));
            System.out.println();
        }
    }

    private void printSolution() {
        for (int i = 0; i < assignments.length; i++) {
            System.out.println(String.format("Node %d (%s) has assignment: %d (%s)", //
                    i, toBinaryString(i), //
                    assignments[i], toBinaryString(twoToThe(assignments[i]))));
            for (final int j : neighboursOf(i)) {
                System.out.println(String.format("  Neighbour %d (%s) has assignment: %d (%s)", //
                        j, toBinaryString(j), //
                        assignments[j], toBinaryString(twoToThe(assignments[j]))));
            }
            if (hasDuplicateNeighbours(i)) {
                throw new IllegalStateException();
            }
        }
    }

    private Object toBinaryString(final int i) {
        return String.format("%" + numberOfCoins + "s", Integer.toBinaryString(i)).replace(' ', '0');
    }

    private void solve() throws FoundSolutionException {

        final int node = findUnassignedNodeWithMostPopulatedNeighbours();
        if (node == -1) {
            throw new FoundSolutionException();
        }
        if (hasDuplicateNeighbours(node)) {
            throw new IllegalStateException();
        }
        for (int value = 0; value < numberOfCoins; value++) {
            assignValueToNode(node, value);
            if (!anyNeighbourHasDuplicateNeighbours(node)) {
                solve();
            }
            unassignValueToNode(node);
        }
    }

    private boolean anyNeighbourHasDuplicateNeighbours(final int node) {
        for (final int neigh : neighboursOf(node)) {
            if (hasDuplicateNeighbours(neigh)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDuplicateNeighbours(final int node) {
        int flags = 0;
        for (final int n : neighboursOf(node)) {
            if (assignments[n] != -1) {
                final int flag = twoToThe(assignments[n]);
                if ((flags & flag) != 0) {
                    return true;
                }
                flags |= flag;
            }
        }
        return false;
    }

    private int findUnassignedNodeWithMostPopulatedNeighbours() {
        int bestNode = -1;
        int bestNodeNeighbourCount = -1;
        for (int node = 0; node < assignments.length; node++) {
            if (assignments[node] == -1) {
                int numberOfPopulatedNeighbours = 0;
                for (final int j : neighboursOf(node)) {
                    if (assignments[j] != -1) {
                        numberOfPopulatedNeighbours++;
                    }
                }

                if (bestNode == -1 || numberOfPopulatedNeighbours > bestNodeNeighbourCount) {
                    bestNode = node;
                    bestNodeNeighbourCount = numberOfPopulatedNeighbours;
                }
            }
        }
        return bestNode;
    }

    private void unassignValueToNode(final int node) {
        assignValueToNode(node, -1);
    }

    private void assignValueToNode(final int node, final int value) {
        assignments[node] = value;
    }

    private int[] neighboursOf(final int node) {
        final int[] result = new int[numberOfCoins];
        for (int i = 0; i < numberOfCoins; i++) {
            result[i] = node ^ twoToThe(i);
        }
        return result;
    }

    private static int twoToThe(final int n) {
        return 1 << n;
    }
}
