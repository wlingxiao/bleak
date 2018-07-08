package goa.swagger.util

import scala.collection.mutable.ListBuffer

object SwaggerContext {

  var suffixResponseFormat = true

  private val classLoaders = ListBuffer.empty[ClassLoader]
  registerClassLoader(this.getClass.getClassLoader)

  def registerClassLoader(cl: ClassLoader) = this.classLoaders += cl

  def loadClass(name: String) = {
    var clazz: Class[_] = null

    for (classLoader <- classLoaders.reverse) {
      if (clazz == null) {
        try {
          clazz = Class.forName(name, true, classLoader)
        } catch {
          case e: ClassNotFoundException =>
        }
      }
    }

    if (clazz == null)
      throw new ClassNotFoundException("class " + name + " not found")

    clazz
  }
}
