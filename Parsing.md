SDOM's parser uses SAX, which means that you can parse XML from any [InputSource](http://www.saxproject.org/apidoc/org/xml/sax/InputSource.html) SAX supports.  The default parser builds a DOM that models the XML such that it contains the entire document (comments, whitespace, etc.).  You can always trim it down after parsing.  If you're concerned about the performance of that, you can instantiate your own parser and give it a BuilderConfiguration to tell it what information it's OK to discard.

[[Validation]] is distinct from parsing in SDOM, so it should be used against a DOM after the parsing is complete.

Parsing a string with the default parser is as simple as calling the "parse" method:

    import org.scalawag.sdom._
    val xml1 = XML.parse("""<outer><inner/></outer>""")

    // -> Document(<outer><inner/></outer>)

Instantiating a Parser with a different configuration is pretty straightforward:

    import org.scalawag.sdom._
    val s = """<outer><inner/><?pi something?><!-- blah --></outer>"""
    XML.parse(s)

    // -> Document(<outer><inner/><?pi something?><!-- blah --></outer>)
    
    val parser = new Parser(BuilderConfiguration.Truest.copy(discardProcessingInstructions = true,discardComments = true))
    parser.parse(s)

    // -> Document(<outer><inner/></outer>)
