SDOM's parser uses SAX, which means that you can parse XML from any [InputSource](http://www.saxproject.org/apidoc/org/xml/sax/InputSource.html) SAX supports.  There's not really any configuration of the parser.  The resulting DOM always contains the entire document (comments, whitespace, etc.) and you can trim it down afterwards.  TODO FIX THIS.

[[Validation]] is distinct from parsing in SDOM, so it should be used against a DOM after the parsing is complete.

Parsing a string is as simple as calling the "parse" method:

    import org.scalawag.sdom._
    val xml1 = XML.parse("""<outer><inner/></outer>""")

    // -> Document(<outer><inner/></outer>)