package candy

opaque type State[S, +A] = S => (A, S)

object State:
  extension [S, A](underlying: State[S, A])

    def run(s: S): (A, S) = underlying(s)

    def map[B](f: A => B): State[S, B] =
      (s: S) =>
        val (a, s2) = underlying(s)
        (f(a), s2)

    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      (s: S) =>
        val (a, s2) = underlying(s)
        val (b, s3) = sb(s2)
        (f(a, b), s3)

    def flatMap[B](f: A => State[S, B]): State[S, B] =
      (s: S) =>
        val (a, s2) = underlying(s)
        f(a)(s2)

    def *>[B](andThen: State[S, B]): State[S, B] =
      flatMap(_ => andThen)

  def apply[S, A](f: S => (A, S)): State[S, A] = f

  def unit[S, A](a: A): State[S, A] = s => (a, s)

  def sequence[S, A](rs: List[State[S, A]]): State[S, List[A]] =
    traverse(rs)(identity)

  def traverse[S, A, B](rs: List[A])(f: A => State[S, B]): State[S, List[B]] =
    rs.foldRight[State[S, List[B]]](unit(Nil)) { (a, stateListB) =>
      f(a).map2(stateListB)(_ :: _)
    }

  def get[S]: State[S, S] = s => (s, s)

  def set[S](s: S): State[S, Unit] = _ => ((), s)

  def modify[S](f: S => S): State[S, Unit] =
    for
      s <- get
      _ <- set(f(s))
    yield ()
