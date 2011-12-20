% :mode=prolog:

% Reverse the elements of a list.

conj([], A, [A]).
conj([H|T], A, [H|C]) :- conj(T, A, C).

my_reverse([], []).
my_reverse([H|T], List) :- reverse(T, A), conj(A, H, List).

%my_reverse([1, 2, 3], What).


% Find the smallest element of a list.

smallest([A], A).
smallest([H|T], H) :- smallest(T, A), H =< A.
smallest([H|T], A) :- smallest(T, A), A < H.

%smallest([1, 9, 8, 4], What).


% Sort the elements of a list.

my_remove([A|T], A, T).
my_remove([H|T], A, [H|C]) :- A \= H, my_remove(T, A, C).
my_sort([], []).
my_sort([H|T], [A|B]) :- smallest([H|T], A), my_remove([H|T], A, C), my_sort(C, B).

%my_sort([1, 9, 8, 4], What).

