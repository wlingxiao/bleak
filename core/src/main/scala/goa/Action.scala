package goa

trait Action extends (Context => Response) {
  override def apply(ctx: Context): Response
}
