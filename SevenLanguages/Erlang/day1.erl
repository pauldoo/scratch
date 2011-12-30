-module(day1).
-export([words_in_string/1]).
-export([count_to/2]).
-export([message/1]).

spaces_in_string([32 | Rem]) -> 1 + spaces_in_string(Rem);
spaces_in_string([H | Rem]) -> spaces_in_string(Rem);
spaces_in_string([]) -> 0.
words_in_string(L) -> 1 + spaces_in_string(L).
% day1:words_in_string("hello world foo bar").

count_to(B, B) -> [];
count_to(A, B) -> [A | count_to(A+1, B)].
% day1:count_to(1, 10).

message(success) -> "success";
message({error, Message}) -> Message.
% day1:message(success).
% day1:message({error, "foobar"}).

