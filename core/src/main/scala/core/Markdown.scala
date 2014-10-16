package core

import org.markdown4j.Markdown4jProcessor

object Markdown {

  def toHtml(markdown: String): String = {
    new Markdown4jProcessor().process(markdown)
  }

}
