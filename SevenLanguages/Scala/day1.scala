class TicTacToe(val cells: List[List[Char]]) {
    def winner():Option[Char] = {
        val l =
            (
                if ( (0 until 3).map( j => (cells(0)(0) == cells(j)(j) ) ).reduce( _&&_ ) )
                    Some(cells(0)(0)) else None
            ) ::
            (
                if ( (0 until 3).map( j => (cells(0)(2) == cells(j)(2-j) ) ).reduce( _&&_ ) )
                    Some(cells(0)(2)) else None
            ) ::
            (
                (0 until 3).map( i => (
                    if ( (0 until 3).map( j => (cells(i)(0) == cells(i)(j) ) ).reduce( _&&_ ) )
                        Some(cells(i)(0)) else None
                )) toList
            ) :::
            (
                (0 until 3).map( i => (
                    if ( (0 until 3).map( j => (cells(0)(i) == cells(j)(i) ) ).reduce( _&&_ ) )
                        Some(cells(0)(i)) else None
                )) toList
            ) : List[Option[Char]];
        l.filter( k => (k != Some(' ')) ).fold(None)( _.orElse(_) );
    }
}

println((new TicTacToe(
    (' ' :: ' ' :: ' ' :: Nil) ::
    (' ' :: ' ' :: ' ' :: Nil) ::
    (' ' :: ' ' :: ' ' :: Nil) :: Nil)) winner);

println((new TicTacToe(
    ('x' :: ' ' :: ' ' :: Nil) ::
    (' ' :: 'x' :: ' ' :: Nil) ::
    (' ' :: ' ' :: 'x' :: Nil) :: Nil)) winner);

println((new TicTacToe(
    (' ' :: ' ' :: 'o' :: Nil) ::
    (' ' :: 'o' :: ' ' :: Nil) ::
    ('o' :: ' ' :: ' ' :: Nil) :: Nil)) winner);

println((new TicTacToe(
    (' ' :: 'x' :: ' ' :: Nil) ::
    (' ' :: 'x' :: 'o' :: Nil) ::
    ('o' :: 'o' :: ' ' :: Nil) :: Nil)) winner);

println((new TicTacToe(
    (' ' :: ' ' :: 'x' :: Nil) ::
    (' ' :: ' ' :: 'x' :: Nil) ::
    (' ' :: ' ' :: 'x' :: Nil) :: Nil)) winner);

println((new TicTacToe(
    ('o' :: 'o' :: 'o' :: Nil) ::
    (' ' :: ' ' :: ' ' :: Nil) ::
    (' ' :: ' ' :: ' ' :: Nil) :: Nil)) winner);

