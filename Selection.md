Once you have a Document, you can use operators to select specific parts of the DOM.  SDOM sports the following operators:

* \ - select children of the LHS Parent node(s) (Document or Element)
* \\ - select descendants of the LHS Parent node(s) (Document or Element)
* \@ - select attributes of the LHS Element node(s)

These operators work on the specific Node types as well as Iterables of those node types.  This allows you to chain several selectors to dig deeper into your DOM.

    import org.scalawag.sdom._
    val x1 = XML.parse("""<a><b id="1"/><b id="2"><c>8</c></b><d><b id="3"/></d></a>""")

    x1.root \ "b"

    // -> Iterable[Element] = List(Element(<b id="1"/>), Element(<b id="2"><c>8</c></b>))

    x1.root \\ "b"

    // -> Iterable[Element] = List(Element(<b id="1"/>), Element(<b id="2"><c>8</c></b>), Element(<b id="3"/>))

    x1.root.childElements.head \@ "id"

    // -> Iterable[Attribute] = Vector(Attribute(id="1"))

    x1 \\ "b" \ "c"

    // -> Iterable[Element] = List(Element(<c>8</c>))

Depending on what you put on the right side of the operator, you'll get a different result type.  There are several built-in selectors:

* * - select all Elements
* node - selects all Nodes
* "name" - selects Elements with the specified name

Here are some more examples:

    x1 \ "a" \ node

    // -> Iterable[Child] = List(Element(<b>1</b>), Element(<b><c>8</c></b>), Element(<d><b>3</b></d>))

Note that the return type ais Iterable[Child] even though all the children are Elements.  This is because they
_could_ have been another type like Text.  Witness:

    x1 \\ "c" \ node

    // -> Iterable[Child] = List(Text(8))

    x1 \\ node

    // -> Iterable[Child] = List(Element(<a><b id="1"/><b id="2"><c>8</c></b><d><b id="3"/></d></a>), Element(<b id="1"/>), Element(<b id="2"><c>8</c></b>), Element(<c>8</c>), Text(8), Element(<d><b id="3"/></d>), Element(<b id="3"/>))

Here are some examples:

    import org.scalawag.sdom._
    val x1 = XML.parse("""<a><b id="1"/><b id="2"><c/></b><d><b id="3"/></d></a>""")

    x1 \\ "b"

    // -> Iterable[Element] = List(Element(<b>1</b>), Element(<b><c>2</c></b>), Element(<b>3</b>))

    x1 \ "a" \ "b" \ "c"
    Iterable[Element] = List(Element(<c>2</c>))

    x1 \ "a" \ node

    // -> Iterable[Child] = List(Element(<b>1</b>), Element(<b><c>2</c></b>), Element(<d><b>3</b></d>))

There is an implicit conversion from String to ElementName.  This means that you can use a pattern similar to what you may be used to with the built-in scala.xml classes to select
