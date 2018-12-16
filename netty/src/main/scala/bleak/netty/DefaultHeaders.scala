package bleak
package netty

import java.util.concurrent.locks.ReentrantReadWriteLock

import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}

import collection.JavaConverters._

private[netty] class DefaultHeaders(httpHeaders: HttpHeaders) extends Headers {

  private val rwl = new ReentrantReadWriteLock()
  private val rlock = rwl.readLock
  private val wlock = rwl.writeLock

  override def getAll(key: String): Iterable[String] = {
    rlock.lock()
    try {
      httpHeaders.getAll(key).asScala
    } finally rlock.unlock()
  }

  override def +=(kv: (String, String)): this.type = {
    wlock.lock()
    try {
      httpHeaders.add(kv._1, kv._2)
      this
    } finally wlock.unlock()
  }

  override def -=(key: String): this.type = {
    wlock.lock()
    try {
      httpHeaders.remove(key)
      this
    } finally wlock.unlock()
  }

  override def get(key: String): Option[String] = {
    rlock.lock()
    try {
      Option(httpHeaders.get(key))
    } finally rlock.unlock()
  }

  override def iterator: Iterator[(String, String)] = {
    rlock.lock()
    try {
      httpHeaders.iteratorAsString().asScala.map { entry =>
        (entry.getKey, entry.getValue)
      }
    } finally rlock.unlock()
  }

  override def add(k: String, v: String): Headers = {
    wlock.lock()
    try {
      httpHeaders.add(k, v)
      this
    } finally wlock.unlock()
  }

  override def set(k: String, v: String): Headers = {
    wlock.lock()
    try {
      httpHeaders.set(k, v)
      this
    } finally wlock.unlock()
  }
}

private[netty] object DefaultHeaders {

  def empty: DefaultHeaders = apply()

  def apply(): DefaultHeaders = {
    new DefaultHeaders(new DefaultHttpHeaders())
  }

  def apply(headers: HttpHeaders): DefaultHeaders = {
    new DefaultHeaders(headers)
  }

}