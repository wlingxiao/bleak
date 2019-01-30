package bleak

import java.util.concurrent.locks.ReentrantReadWriteLock

import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}

import scala.collection.JavaConverters._
import scala.collection.mutable

trait Headers extends Params[String] with mutable.Map[String, String] {

  def add(k: String, v: String): Headers

  def set(k: String, v: String): Headers
}

object Headers {

  def apply(): Headers =
    apply(Nil)

  def apply(headers: Iterable[(String, String)]): Headers = {
    val header = new Impl(new DefaultHttpHeaders())
    for ((name, value) <- headers) {
      header.add(name, value)
    }
    header
  }

  def apply(httpHeaders: HttpHeaders): Headers = new Impl(httpHeaders)

  final class Impl(httpHeaders: HttpHeaders) extends Headers {

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

}
