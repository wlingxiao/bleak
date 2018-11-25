package bleak

trait Module {

  def init(app: App): Unit = {}

  def destroy(app: App): Unit = {}

}
