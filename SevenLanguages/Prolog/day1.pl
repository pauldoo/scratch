% :mode=prolog:

% Books and authors

wrote(bruce, seven_languages).
wrote(houser, joy_of_clojure).
wrote(fogus, joy_of_clojure).
wrote(okasaki, pfds).
wrote(harold, sicp).
wrote(gerald, sicp).


% Find books with single author

multiple_authors(X) :- wrote(A, X), wrote(B, X), A \= B.
single_author(X) :- wrote(_, X), \+(multiple_authors(X)).

%single_author(Book).


% Musicians and instruments / genre

plays(alice, guitar).
plays(bob, piano).
plays(charlie, guitar).
plays(charlie, piano).

genre(alice, rock).
genre(bob, rock).
genre(charlie, jazz).


% Find all musicians who play guitar
%plays(Who, guitar).

