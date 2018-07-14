package goa.swagger.util

import scala.collection.mutable.ListBuffer

object SwaggerContext {
  private val classLoaders = ListBuffer.empty[ClassLoader]

  registerClassLoader(getClass.getClassLoader)

  private def registerClassLoader(cl: ClassLoader): Unit = {
    classLoaders += cl
  }

  def loadClass(name: String): Class[_] = {
    var clazz: Class[_] = null
    for (classLoader <- classLoaders.reverse) {
      if (clazz == null) {
        try {
          clazz = Class.forName(name, true, classLoader)
        } catch {
          case e: ClassNotFoundException => throw e
        }
      }
    }
    if (clazz == null)
      throw new ClassNotFoundException("class " + name + " not found")
    clazz
  }
}
