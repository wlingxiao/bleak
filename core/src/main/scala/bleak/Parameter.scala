package bleak

import bleak.Params._

trait Parameter {

  def paths: PathParams

  def args: QueryParams

  def form: FormParams

  def files: FormFileParams

}
