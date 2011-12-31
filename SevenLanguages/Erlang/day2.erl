-module(day2).
-export([lookup/2]).
-export([compute_prices/1]).
-export([winner/1]).

lookup(Key, [{Key, Value} | _]) -> Value;
lookup(Key, [_ | T]) -> lookup(Key, T).
% day2:lookup(c, [{a, 1}, {b, 2}, {c, 3}, {d, 4}, {e, 5}]).

compute_prices(L) -> [{Item, Quantity*Price} || {Item, Quantity, Price} <- L].
% day2:compute_prices([{cheese, 3, 2}, {onions, 10, 1}]).

concat([], B) -> B;
concat([H | T], B) -> concat(T, [H | B]).

reduce([H], _) -> H;
reduce([A, B | T], F) -> reduce([F(A, B) | T], F).

pick(_, _, _, 0) -> [];
pick([H | T], 0, S, N) -> [H | pick(T, S-1, S, N-1)];
pick([_ | T], I, S, N) -> pick(T, I-1, S, N).

super_pick(L, I, S, SS) -> [pick(L, I, S, 3), pick(L, I+SS, S, 3), pick(L, I+SS+SS, S, 3)].

find_winner([[x, x, x] | _]) -> x;
find_winner([[o, o, o] | _]) -> o;
find_winner([_ | T]) -> find_winner(T);
find_winner([]) -> no_winner.

has_spaces([], _, B) -> B;
has_spaces([x | T], A, B) -> has_spaces(T, A, B);
has_spaces([o | T], A, B) -> has_spaces(T, A, B);
has_spaces([_ | _], A, _) -> A.

shizzle(no_winner, L) -> has_spaces(L, no_winner, cat);
shizzle(A, _) -> A.

winner(L) ->
    shizzle(
        find_winner(
            reduce(
                [
                    [pick(L, 0, 4, 3)],
                    [pick(L, 2, 2, 3)],
                    super_pick(L, 0, 3, 1),
                    super_pick(L, 0, 1, 3)
                ],
                fun(A, B) -> concat(A, B) end)),
        L).

%day2:winner([x, e, e, e, x, e, e, e, x]).
%day2:winner([x, e, e, e, x, e, e, e, e]).
%day2:winner([x, o, o, o, o, x, x, x, o]).

