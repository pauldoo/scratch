% :mode=prolog:

% 9x9 sudoku

my_length([], 0).
my_length([_|T], L) :- my_length(T, A), L is A + 1.

my_all_different([]).
my_all_different([A|B]) :-
    fd_all_different(A),
    my_all_different(B).

my_pick_element([H|_], 0, H).
my_pick_element([_|T], A, E) :- A > 0, B is A - 1, my_pick_element(T, B, E).

my_single_pick(_, _, _, 3, _, _, []).
my_single_pick(L, O, 3, IY, SX, SY, R) :-
    IY < 3,
    NY is IY + 1,
    my_single_pick(L, O, 0, NY, SX, SY, R).
my_single_pick(L, O, IX, IY, SX, SY, [H|T]) :-
    O < 81, IX < 3, IY < 3,
    I is O + (IX * SX) + (IY * SY),
    my_pick_element(L, I, H),
    NX is IX + 1,
    my_single_pick(L, O, NX, IY, SX, SY, T).

my_multi_pick(_, _, _, [], []).
my_multi_pick(L, SX, SY, [A|B], [H|T]) :-
    my_single_pick(L, A, 0, 0, SX, SY, H),
    my_multi_pick(L, SX, SY, B, T).

sudoku(Solution, Puzzle) :-
    Solution = Puzzle,
    my_length(Solution, 81),
    fd_domain(Solution, 1, 9),

    ColumnOffsets = [0, 1, 2, 3, 4, 5, 6, 7, 8],
    RowOffsets = [0, 9, 18, 27, 36, 45, 54, 63, 72],
    BoxOffsets = [0, 3, 6, 27, 30, 33, 54, 57, 60],
    my_multi_pick(Solution, 1, 3, RowOffsets, Rows),
    my_multi_pick(Solution, 9, 27, ColumnOffsets, Columns),
    my_multi_pick(Solution, 1, 9, BoxOffsets, Boxes),

    my_all_different(Rows),
    my_all_different(Columns),
    my_all_different(Boxes).




