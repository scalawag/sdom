Once you have a Document, you can use operators to select specific parts of the DOM.  SDOM sports the following operators:

* \ - select children of the LHS Parent node(s) (Document or Element)
* \\ - select descendants of the LHS Parent node(s) (Document or Element)

Depending on what you put on the right side of the operator, you'll get a different result type.  There are several built-in selectors:

* * - select all Elements
* node - selects all Nodes
* ElementName - selects Elements with the specified name

    import org.scalawag.sdom._
    val x1 = XML.parse("<a><b>1</b><b><c>2</c></b><d><b>3</b></d></a>")

    x1 \\ "b"

    // -> Iterable[Element] = List(Element(<b>1</b>), Element(<b><c>2</c></b>), Element(<b>3</b>))

    x1 \ "a" \ node

    // -> Iterable[Child] = List(Element(<b>1</b>), Element(<b><c>2</c></b>), Element(<d><b>3</b></d>))

There is an implicit conversion from String to ElementName.  This means that you can use a pattern similar to what you may be used to with the built-in scala.xml classes to select
