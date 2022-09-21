package zio.http.internal

import zio._

trait ZKeyedPool[+Err, -Key, Item] {

  /**
   * Retrieves an item from the pool belonging to the given key in a scoped
   * effect. Note that if acquisition fails, then the returned effect will fail
   * for that same reason. Retrying a failed acquisition attempt will repeat the
   * acquisition attempt.
   */
  def get(key: Key)(implicit trace: Trace): ZIO[Scope, Err, Item]

  /**
   * Invalidates the specified item. This will cause the pool to eventually
   * reallocate the item, although this reallocation may occur lazily rather
   * than eagerly.
   */
  def invalidate(item: Item)(implicit trace: Trace): UIO[Unit]

}

object ZKeyedPool {

  /**
   * Makes a new pool of the specified fixed size. The pool is returned in a
   * `Scope`, which governs the lifetime of the pool. When the pool is shutdown
   * because the `Scope` is closed, the individual items allocated by the pool
   * will be released in some unspecified order.
   */
  def make[Key, Env: EnvironmentTag, Err, Item](get: Key => ZIO[Env, Err, Item], size: => Int)(implicit
    trace: Trace,
  ): ZIO[Env with Scope, Nothing, ZKeyedPool[Err, Key, Item]] =
    make(get, _ => size)

  /**
   * Makes a new pool of the specified fixed size. The pool is returned in a
   * `Scope`, which governs the lifetime of the pool. When the pool is shutdown
   * because the `Scope` is closed, the individual items allocated by the pool
   * will be released in some unspecified order.
   *
   * The size of the underlying pools can be configured per key.
   */
  def make[Key, Env: EnvironmentTag, Err, Item](get: Key => ZIO[Env, Err, Item], size: Key => Int)(implicit
    trace: Trace,
  ): ZIO[Env with Scope, Nothing, ZKeyedPool[Err, Key, Item]] = {
    makeWith(get, (key: Key) => { val s = size(key); s to s })(_ => None)
  }

  /**
   * Makes a new pool with the specified minimum and maximum sizes and time to
   * live before a pool whose excess items are not being used will be shrunk
   * down to the minimum size. The pool is returned in a `Scope`, which governs
   * the lifetime of the pool. When the pool is shutdown because the `Scope` is
   * used, the individual items allocated by the pool will be released in some
   * unspecified order.
   */
  def make[Key, Env: EnvironmentTag, Err, Item](
    get: Key => ZIO[Env, Err, Item],
    range: => Range,
    timeToLive: => Duration,
  )(implicit
    trace: Trace,
  ): ZIO[Env with Scope, Nothing, ZKeyedPool[Err, Key, Item]] =
    make(get, _ => range, _ => timeToLive)

  /**
   * Makes a new pool with the specified minimum and maximum sizes and time to
   * live before a pool whose excess items are not being used will be shrunk
   * down to the minimum size. The pool is returned in a `Scope`, which governs
   * the lifetime of the pool. When the pool is shutdown because the `Scope` is
   * used, the individual items allocated by the pool will be released in some
   * unspecified order.
   *
   * The size of the underlying pools can be configured per key.
   */
  def make[Key, Env: EnvironmentTag, Err, Item](
    get: Key => ZIO[Env, Err, Item],
    range: Key => Range,
    timeToLive: Key => Duration,
  )(implicit
    trace: Trace,
  ): ZIO[Env with Scope, Nothing, ZKeyedPool[Err, Key, Item]] =
    makeWith(get, range)((key: Key) => Some(timeToLive(key)))

  private def makeWith[Key, Env: EnvironmentTag, Err, Item](get: Key => ZIO[Env, Err, Item], range: Key => Range)(
    ttl: Key => Option[Duration],
  )(implicit
    trace: Trace,
  ): ZIO[Env with Scope, Nothing, ZKeyedPool[Err, Key, Item]] =
    for {
      environment <- ZIO.environment[Env]
      fiberId     <- ZIO.fiberId
      scope       <- ZIO.scope
      activePools <- Ref.make(Map.empty[Key, Promise[Nothing, ZPool[Err, Item]]])
      getOrCreatePool = (key: Key) =>
        activePools.modify { map =>
          map.get(key) match {
            case Some(promise) => promise.await -> map
            case None          =>
              val promise = Promise.unsafe.make[Nothing, ZPool[Err, Item]](fiberId)(Unsafe.unsafe)
              scope
                .extend(
                  ZPool
                    .make(get(key).provideEnvironment(environment), range(key), ttl(key).getOrElse(Duration.Infinity)),
                )
                .intoPromise(promise) *> promise.await -> (map + (key -> promise))
          }
        }.flatten
    } yield DefaultKeyedPool(activePools, getOrCreatePool)

  private final case class DefaultKeyedPool[Err, Key, Item](
    activePools: Ref[Map[Key, Promise[Nothing, ZPool[Err, Item]]]],
    getOrCreatePool: Key => ZIO[Any, Nothing, ZPool[Err, Item]],
  ) extends ZKeyedPool[Err, Key, Item] {

    override def get(key: Key)(implicit trace: Trace): ZIO[Scope, Err, Item] =
      for {
        pool <- getOrCreatePool(key)
        item <- pool.get
      } yield item

    override def invalidate(item: Item)(implicit trace: Trace): UIO[Unit] =
      activePools.get.flatMap { map =>
        ZIO.foreachDiscard(map.values)(_.await.flatMap(_.invalidate(item)))
      }
  }
}
