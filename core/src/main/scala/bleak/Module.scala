package bleak

trait Module {

  def init(app: Application): Unit = {}

  def destroy(app: Application): Unit = {}

}
