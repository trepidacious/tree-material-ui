package org.rebeam.tree.view.markdown

import japgolly.scalajs.react._

import scala.scalajs.js

object MarkdownView {

  sealed trait Element

  /**
    * Inline HTML
    */
  case object HtmlInline extends Element

  /**
    * Block of HTML
    */
  case object HtmlBlock extends Element

  /**
    * Text nodes (inside of paragraphs, list items etc)
    */
  case object Text extends Element

  /**
    * Paragraph nodes (<p>)
    */
  case object Paragraph extends Element

  /**
    * Headers (<h1>, <h2> etc)
    */
  case object Heading extends Element

  /**
    * Newlines
    */
  case object Softbreak extends Element

  /**
    * Hard line breaks (<br>)
    */
  case object Hardbreak extends Element

  /**
    * Link nodes (<a>)
    */
  case object Link extends Element

  /**
    * Image nodes (<img>)
    */
  case object Image extends Element

  /**
    * Emphasis nodes (<em>)
    */
  case object Emph extends Element

  /**
    * Inline code nodes (<code>)
    */
  case object Code extends Element

  /**
    * Blocks of code (<code>)
    */
  case object CodeBlock extends Element

  /**
    * Block quotes (<blockquote>)
    */
  case object BlockQuote extends Element

  /**
    * List nodes (<ol>, <ul>)
    */
  case object List extends Element

  /**
    * List item nodes (<li>)
    */
  case object Item extends Element

  /**
    * Strong/bold nodes (<strong>)
    */
  case object Strong extends Element

  /**
    * Horizontal rule nodes (<hr>)
    */
  case object ThematicBreak extends Element

  /**
    * Props for Markdown
    * @param source The Markdown source to parse (required)
    * @param className Class name of the container element (default if None: '').
    * @param containerTagName Tag name for the container element, since Markdown can have many root-level
    *                         elements, the component need to wrap them in something (default: div).
    * @param escapeHtml Setting to true will escape HTML blocks, rendering plain text instead of inserting
    *                   the blocks as raw HTML (default if None: false).
    *                   Note that in Scala wrapper we default this to Some(true), to escape HTML for safety.
    * @param skipHtml Setting to true will skip inlined and blocks of HTML (default if None: false).
    * @param sourcePos Setting to true will add data-sourcepos attributes to all elements, indicating where
    *                  in the markdown source they were rendered from (default if None: false).
    * @param softBreak Setting to br will create <br> tags instead of newlines (default if None: \n).
    * @param allowedTypes Defines which types of nodes should be allowed (rendered). (default if None: all types).
    * @param disallowedTypes Defines which types of nodes should be disallowed (not rendered). (default if None: none).
    * @param unwrapDisallowed Setting to true will try to extract/unwrap the children of disallowed nodes.
    *                         For instance, if disallowing Strong, the default behaviour is to simply skip
    *                         the text within the strong altogether, while the behaviour some might want is
    *                         to simply have the text returned without the strong wrapping it. (default if None: false)
    */
  def apply(
    source:               String,
    className:            Option[String] = None,
    containerTagName:     Option[String] = None,
//    containerProps - object An object containing custom element props to put on the container element such as id and htmlFor.
//    childBefore - object A single child object that is rendered before the markdown source but within the container element
//    childAfter - object A single child object that is rendered after the markdown source but within the container element
    escapeHtml:           Option[Boolean] = Some(true),
    skipHtml:             Option[Boolean] = None,
    sourcePos:            Option[Boolean] = None,
    softBreak:            Option[String] = None,
    allowedTypes:         List[Element] = Nil,
    disallowedTypes:      List[Element] = Nil,
    unwrapDisallowed:     Option[Boolean] = None
//    allowNode - function Function execute if in order to determine if the node should be allowed. Ran prior to checking allowedTypes/disallowedTypes. Returning a truthy value will allow the node to be included. Note that if this function returns true and the type is not in allowedTypes (or specified as a disallowedType), it won't be included. The function will get a single object argument (node), which includes the following properties:
//        type - string The type of node - same ones accepted in allowedTypes and disallowedTypes
//        renderer - string The resolved renderer for this node
//        props - object Properties for this node
//        children - array Array of children
//    renderers - object An object where the keys represent the node type and the value is a React component. The object is merged with the default renderers. The props passed to the component varies based on the type of node. See the type renderer options of commonmark-react-renderer for more details.
//    transformLinkUri - function|null Function that gets called for each encountered link with a single argument - uri. The returned value is used in place of the original. The default link URI transformer acts as an XSS-filter, neutralizing things like javascript:, vbscript: and file: protocols. If you specify a custom function, this default filter won't be called, but you can access it as require('react-markdown').uriTransformer. If you want to disable the default transformer, pass null to this option.
//    transformImageUri - function|null Function that gets called for each encountered image with a single argument - uri. The returned value is used in place of the original.
  ): ReactComponentU_ = {

    def elementToJS(e: Element) = e match {
      case HtmlInline     => "HtmlInline"
      case HtmlBlock      => "HtmlBlock"
      case Text           => "Text"
      case Paragraph      => "Paragraph"
      case Heading        => "Heading"
      case Softbreak      => "Softbreak"
      case Hardbreak      => "Hardbreak"
      case Link           => "Link"
      case Image          => "Image"
      case Emph           => "Emph"
      case Code           => "Code"
      case CodeBlock      => "CodeBlock"
      case BlockQuote     => "BlockQuote"
      case List           => "List"
      case Item           => "Item"
      case Strong         => "Strong"
      case ThematicBreak  => "ThematicBreak"
    }

    val f = React.asInstanceOf[js.Dynamic].createFactory(js.Dynamic.global.ReactMarkdown) // access real js component

    val p = js.Dynamic.literal(
      "source" -> source
    )
    className.foreach(p.updateDynamic("className")(_))
    containerTagName.foreach(p.updateDynamic("containerTagName")(_))
    escapeHtml.foreach(p.updateDynamic("escapeHtml")(_))
    skipHtml.foreach(p.updateDynamic("skipHtml")(_))
    sourcePos.foreach(p.updateDynamic("sourcePos")(_))
    softBreak.foreach(p.updateDynamic("softBreak")(_))
    if (allowedTypes.nonEmpty) {
      p.updateDynamic("allowedTypes")(allowedTypes.map(elementToJS))
    }
    if (disallowedTypes.nonEmpty) {
      p.updateDynamic("disallowedTypes")(disallowedTypes.map(elementToJS))
    }
    unwrapDisallowed.foreach(p.updateDynamic("unwrapDisallowed")(_))

    f(p).asInstanceOf[ReactComponentU_]
  }
}
