package transform

trait JsonSerializer[TInput, TOutput] {
  def asJson(input: TInput): TOutput
  def arrayAsJson(input: List[TInput]): TOutput
}
