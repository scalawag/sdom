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

* * - select all Elements (Iterable[Element])
* node - selects all Nodes (Iterable[Child])
* ElementName - selects Elements with the specified name

Here are some more examples:

    x1 \ "a" \ node

    // -> Iterable[Child] = List(Element(<b>1</b>), Element(<b><c>8</c></b>), Element(<d><b>3</b></d>))

Note that the return type above is Iterable[Child] even though all the children are Elements.  This is because they
_could_ have been another type like Text.

    x1 \\ "c" \ node

    // -> Iterable[Child] = List(Text(8))

    x1 \\ node

    // -> Iterable[Child] = List(Element(<a><b id="1"/><b id="2"><c>8</c></b><d><b id="3"/></d></a>), Element(<b id="1"/>), Element(<b id="2"><c>8</c></b>), Element(<c>8</c>), Text(8), Element(<d><b id="3"/></d>), Element(<b id="3"/>))

    x1 \\ * \@ "id"

    // -> Iterable[Attribute] = List(Attribute(id="1"), Attribute(id="2"), Attribute(id="3"))

# Namespaces

## Elements

As you may have noticed above, there is an implicit conversion from String to ElementName.  This means that you can use a pattern similar to what you may be used to with the built-in scala.xml classes to select child elements with a specific name.  One thing that SDOM adds is better namespace support.  It gives you the ability to search within specific namespaces using a pattern similar to that used within XML itself.  To search for elements by their full name, you can either be explicit:

    val x2 = XML.parse("""
      <a xmlns="A">
        <b xmlns="B" id="1"/>
        <b xmlns="C" id="2">
          <c>8</c>
        </b>
        <d>
          <b id="3"/>
        </d>
      </a>
    """)

    x2 \ "a"

    // -> Iterable[Child] = List()

This no longer returns anything because it's looking for an element with local name "a" in the namespace "". Our root element is in the namespace "A".  We can once again find the root element by qualifying it with its namespace.

    x2 \ ElementName("a","A")

    // -> Iterable[Child] = List(Element(<a xmlns="A">...</a>)

That's a perfectly fine way to address the root element.  There are several others.

    {
      // Use of a prefixed namespace in an implicit Namespaces.
      implicit val namespaces = Namespaces("p" -> "A")
      x2 \ "p:a"
    }

    // -> Iterable[Child] = List(Element(<a xmlns="A">...</a>)

    {
      // Use of a the default namespace in an implicit Namespaces.
      implicit val namespaces = Namespaces("" -> "A")
      x2 \ "a"
    }

    // -> Iterable[Child] = List(Element(<a xmlns="A">...</a>)

    // Use of an explicit namespace (not a prefix)
    x2 \ "{A}a"

    // -> Iterable[Child] = List(Element(<a xmlns="A">...</a>)
    
There is no way to query for the specific prefix used in an XML document.  This is against the philosophy of SDOM which says that the prefix is not semantically significant outside of the document.  It's only used _within the serialized XML document_ to map to a namespace which is valid outside the document.  These selectors are outside the document and have their own prefix mapping within your source code that has nothing to do with the mapping in the document.

## Attributes

Note that, according to the XML specification, attributes with no prefix belong to the namespace "" and not the default prefix.  SDOM behaves consistently with this behavior in its selectors.  You have to be explicit about attribute namespaces even if there is an implicit Namespaces in scope that sets the default namespace.

    val x3 = XML.parse("""<a xmlns:n="NS1"><b n:id="2" id="3"/></a>""")

    {
      implicit val namespaces = Namespaces("" -> "NS1")
      // We have to explicit search for element "b" in the "" namespace or else it will use the default.
      x3 \\ "{}b" \@ "id"
    }

    // -> Iterable[Attribute] = List(Attribute(id="3"))

That selects the "id" attribute in the "" namespace.  Of course, the prefixed and explicit style names still work with attributes.

    {
      implicit val namespaces = Namespaces("p" -> "NS1")
      x3 \\ "b" \@ "p:id"
    }

    // -> Iterable[Attribute] = List(Attribute(id="2"))

    x3 \\ "b" \@ "{NS1}id"

    // -> Iterable[Attribute] = List(Attribute(id="2"))

    x3 \\ "b" \@ AttributeName("id","NS1")

    // -> Iterable[Attribute] = List(Attribute(id="2"))
